package evolution.dto;

import java.util.concurrent.Future;

import org.apache.http.HttpResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Task {
	private Integer beginDataIndex;
	private Integer endDataIndex;
	private Boolean success;
	private Future<HttpResponse> response;
	
	public Task(Integer beginDataIndex, Integer endDataIndex, Boolean success) {
		this.beginDataIndex = beginDataIndex;
		this.endDataIndex = endDataIndex;
		this.success = success;
	}
}
