package com.pink.pfa.context;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.pink.pfa.config.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PfaContextTest extends PfaBase {

	@Test
	void contextLoads() {
	}

}
