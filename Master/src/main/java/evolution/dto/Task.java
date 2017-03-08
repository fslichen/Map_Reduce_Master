package evolution.dto;

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Task {
	private String taskUrl;
	private Integer beginDataIndex;
	private Integer endDataIndex;
	private Boolean success;
	private Future<HttpResponse> response;
	
	public Task(String taskUrl, Integer beginDataIndex, Integer endDataIndex) {
		this.taskUrl = taskUrl;
		this.beginDataIndex = beginDataIndex;
		this.endDataIndex = endDataIndex;
	}
}
