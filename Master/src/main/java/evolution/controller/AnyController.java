package evolution.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import evolution.service.AnyService;

@RestController
public class AnyController {
	@Autowired
	private AnyService anyService;
	
	@PostMapping("/mapReduce")
	public void mapReduce(@RequestBody List<String> data) {
		Map<String, Double> map = anyService.mapReduce(data);
		System.out.println(map);
	}
}
