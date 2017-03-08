package evolution.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import evolution.service.AnyService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AnyController {
	@Autowired
	private AnyService anyService;
	
	@PostMapping("/mapReduce")
	public Map<String, Double> reduce(@RequestBody List<String> data) {
		Map<String, Double> dataCount = anyService.mapReduce(data);
		log.info("The data count is {}.", dataCount);
		return dataCount;
	}
}
