package ejat_common.ejat_test;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppTest {

	@Test
	public void test1() {
		assertEquals(2, 2);
	}
	
	@Test
	public void test2() {
		assertEquals(App.testMethod(), "test string");
	}

}
