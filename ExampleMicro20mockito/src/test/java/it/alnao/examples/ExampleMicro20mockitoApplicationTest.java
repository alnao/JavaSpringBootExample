package it.alnao.examples;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleMicro20mockitoApplication.class)
public class ExampleMicro20mockitoApplicationTest {
	@Test
	public void contextLoads() {
	}


	@Test
	public void applicationContextTest() {
		ExampleMicro20mockitoApplication.main(new String[] {});
	}
}
