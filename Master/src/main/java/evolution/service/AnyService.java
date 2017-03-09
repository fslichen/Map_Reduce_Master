package evolution.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
	
	public static String taskPath;
	
	static {
		taskPath = "/mapReduce";
		clusterHosts = new LinkedList<>();
		clusterHosts.add("http://192.168.0.101:8080");
		clusterHosts.add("http://192.168.0.105:8080");
	}
	
	public String getRandomTaskUrlExcept(String taskUrl) {
		String randomTaskUrl;
		do {
			randomTaskUrl = clusterHosts.get(ThreadLocalRandom.current().nextInt(0, clusterHosts.size())) + taskPath;
		} while (randomTaskUrl.equals(taskUrl));
		return randomTaskUrl;
	}
	
	public List<Task> getTasks(int dataCount) {
		int beginIndex, endIndex = -1;
		int clusterCount = clusterHosts.size();
		int dataSizePerCluster = dataCount / clusterCount;
		List<Task> tasks = new LinkedList<>();
		for (int i = 0; i < clusterCount; i++) {
			beginIndex = endIndex + 1;
			endIndex = beginIndex + dataSizePerCluster;
			endIndex = endIndex < dataCount - 1 ? endIndex : dataCount - 1;
			tasks.add(new Task(getRandomTaskUrlExcept(null), beginIndex, endIndex));
		}
		return tasks;
	}

	public void map(List<String> data, List<Task> tasks) {
		for (Task task : tasks) {
			String taskUrl = task.getTaskUrl();
			List<String> partialData = new ArrayList<>();
			for (int i = task.getBeginDataIndex(); i <= task.getEndDataIndex(); i++) {
				partialData.add(data.get(i));
			}
			task.setResponse(Sender.post(taskUrl, partialData));
		}
	}
	
	public Map<String, Double> mapReduce(List<String> data) {
		List<Task> tasks = getTasks(data.size());
		Map<String, Double> counts = new LinkedHashMap<>();
		do {
			map(data, tasks);
			tasks = reduce(tasks, counts);
		} while (tasks != null && tasks.size() > 0);
		return counts;
	}
	
	public List<Task> reduce(List<Task> tasks, Map<String, Double> counts) {
		List<Task> unsuccessfulTasks = new LinkedList<>();
		tasks.forEach(x -> x.setSuccess(null));// Set the success status as null by default.
		while (tasks != null && tasks.size() > 0) {// Tasks not Handled
			tasks = tasks.stream().filter(x -> x.getSuccess() == null).collect(Collectors.toList());
			for (Task task : tasks) {
				Future<HttpResponse> response = task.getResponse();
				if (response.isDone()) {
					Map<String, Double> partialCounts = Sender.getMap(response, String.class, Double.class);
					if (partialCounts == null) {// The response is corrupted.
						task.setSuccess(false);
						task.setTaskUrl(getRandomTaskUrlExcept(task.getTaskUrl()));// Select another cluster to do the task.
						unsuccessfulTasks.add(task);
						log.error("The unsuccessful task is {}.", task);
					} else {
						task.setSuccess(true);
						MapUtil.updateCount(partialCounts, counts);
					}
				}
			}
		}
		return unsuccessfulTasks;
	}
}
