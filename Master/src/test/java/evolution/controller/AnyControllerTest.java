package evolution.controller;

import java.util.Arrays;

import org.junit.Test;

import evolution.util.Sender;

public class AnyControllerTest {
	@Test
	public void testMapReduce() {
		Sender.post("http://localhost:8080/mapReduce", 
				Arrays.asList("apple", "pear", "apple", "banana", "apple", "pear", "pear"));
	}
}	
