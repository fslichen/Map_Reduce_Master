package evolution.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import evolution.dto.Task;
import evolution.util.MapUtil;
import evolution.util.Sender;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AnyService {
	public static List<String> clusterHosts;
	
	public static String taskUrl;
	
	static {
		taskUrl = "/mapReduce";
		clusterHosts = new LinkedList<>();
		clusterHosts.add("http://192.168.0.101:8080");
		clusterHosts.add("http://192.168.0.107:8080");
	}
	
	public Map<String, Task> getTasks(int dataCount) {
		int beginIndex, endIndex = -1;
		int clusterCount = clusterHosts.size();
		int dataSizePerCluster = dataCount / clusterCount;
		Map<String, Task> tasks = new LinkedHashMap<>();
		for (int i = 0; i < clusterCount; i++) {
			beginIndex = endIndex + 1;
			endIndex = beginIndex + dataSizePerCluster;
			endIndex = endIndex < dataCount - 1 ? endIndex : dataCount - 1;
			tasks.put(clusterHosts.get(i) + taskUrl, new Task(beginIndex, endIndex, null));
		}
		return tasks;
	}

	public void map(List<String> data, Map<String, Task> taskMap) {
		Map<String, Future<HttpResponse>> responseMap = new LinkedHashMap<>();
		for (Entry<String, Task> taskEntry : taskMap.entrySet()) {
			String url = taskEntry.getKey();
			Task task = taskEntry.getValue();
			List<String> partialData = new ArrayList<>();
			for (int i = task.getBeginDataIndex(); i <= task.getEndDataIndex(); i++) {
				partialData.add(data.get(i));
			}
			responseMap.put(url, Sender.post(url, partialData));
		}
		for (Entry<String, Future<HttpResponse>> responseEntry : responseMap.entrySet()) {
			Task task = taskMap.get(responseEntry.getKey());
			task.setResponse(responseEntry.getValue());
		}
	}
	
	public Map<String, Double> mapReduce(List<String> data) {
		Map<String, Task> taskMap = getTasks(data.size());
		map(data, taskMap);
		Map<String, Double> counts = new LinkedHashMap<>();
		reduce(taskMap, counts);
		return counts;
	}
	
	public void reduce(Map<String, Task> taskMap, Map<String, Double> counts) {
		List<Task> unsuccessfulTasks = new LinkedList<>();
		while (taskMap.size() > 0) {
			List<String> doneTaskUrls = new LinkedList<>();
			for (Entry<String, Task> taskEntry : taskMap.entrySet()) {
				Future<HttpResponse> response = taskEntry.getValue().getResponse();
				if (response.isDone()) {
					Map<String, Double> partialCounts = Sender.getMap(response, String.class, Double.class);
					if (partialCounts == null) {
						unsuccessfulTasks.add(taskEntry.getValue());
					}
					MapUtil.updateCount(partialCounts, counts);
					doneTaskUrls.add(taskEntry.getKey());
				}
			}
			for (String doneTaskUrl : doneTaskUrls) {
				taskMap.remove(doneTaskUrl);
			}
		}
		for (Task unsuccessfulTask : unsuccessfulTasks) {
			log.info("The unsuccessful task is {}.", unsuccessfulTask);
		}
	}
}
