package ejat_common.ejat_test;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppTest {
	public String testString = "test string";

	@Test
	public void test1() {
		assertEquals(2, 2);
	}
	
	@Test
	public void test2() {
		assertEquals(App.testMethod1(), testString + "1");
	}
	
	@Test
	public void test3() {
		assertEquals(App.testMethod2(), testString + "2");
	}
	
	@Test
	public void test4() {
		assertEquals(App.testMethod3(), testString + "3");
	}
	
	@Test
	public void test5() {
		assertEquals(App.testMethod4(), testString + "4");
	}

}
