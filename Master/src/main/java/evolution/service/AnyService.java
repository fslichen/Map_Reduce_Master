package evolution.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import evolution.util.MapUtil;
import evolution.util.Sender;

@Service
public class AnyService {
	public static List<String> clusterHosts;
	
	public static String taskUrl;
	
	static {
		taskUrl = "/";
		clusterHosts = new LinkedList<>();
		clusterHosts.add("http://192.168.0.101:8080");
		clusterHosts.add("http://192.168.1.107:8080");
	}
	
	public List<Integer> getTaskIndexes(int taskCount, int clusterCount) {
		List<Integer> indexes = new LinkedList<>();
		indexes.add(0);
		int clusterSize = taskCount / clusterCount;
		for (int i = 0; i < clusterCount; i++) {
			int candidateIndex = indexes.get(i) + clusterSize;
			indexes.add(candidateIndex < taskCount - 1 ? candidateIndex : taskCount - 1);
		}
		return indexes;
	}

	public Map<String, Integer> mapReduce(List<String> data) {
		// Map
		int taskCount = clusterHosts.size();
		List<Integer> taskIndexes = getTaskIndexes(data.size(), taskCount);
		List<Future<HttpResponse>> responses = new LinkedList<>();
		for (int i = 0; i < taskIndexes.size() - 1; i++) {
			int beginIndex = i == 0 ? taskIndexes.get(i) : taskIndexes.get(i) + 1;
			int endIndex = taskIndexes.get(i + 1);	
			String url = clusterHosts.get(i) + taskUrl;
			List<String> partialData = new ArrayList<>();
			for (int j = beginIndex; j <= endIndex; j++) {
				partialData.add(data.get(j));
			}
			responses.add(Sender.post(url, partialData));
		}
		// Reduce
		int doneTaskCount = 0;
		Map<String, Integer> summary = new HashMap<>();
		while (doneTaskCount < taskCount) {
			for (Future<HttpResponse> response : responses) {
				if (response.isDone()) {
					Map<String, Integer> partialSummary = Sender.getMap(response, String.class, Integer.class);
					MapUtil.updateCount(partialSummary, summary);
					doneTaskCount++;
				}
			}
		}
		return summary;
	}
}
