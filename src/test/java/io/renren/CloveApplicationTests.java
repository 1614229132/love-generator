package io.renren;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CloveApplicationTests.class)
public class CloveApplicationTests {

	@Test
	public void contextLoads() {
		int i = 1+1 ;
		System.out.println(i);
	}

}
