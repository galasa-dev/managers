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
		assertEquals(App.testMethod1(), "test string");
	}
	
	@Test
	public void test3() {
		assertEquals(App.testMethod2(), "test string");
	}
	
	@Test
	public void test4() {
		assertEquals(App.testMethod3(), "test string");
	}
	
	@Test
	public void test5() {
		assertEquals(App.testMethod4(), "test string");
	}

}
