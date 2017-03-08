package evolution.service;

import org.junit.Test;

public class AnyServiceTest {
	private AnyService anyService = new AnyService();
	
	@Test
	public void testGetTaskIndexes() {
		System.out.println(anyService.getTasks(12));
	}
}
