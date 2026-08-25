package com.kyf.mp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

	@Test
	@DisplayName("Spring 应用上下文应能正常启动")
	void contextLoads() {
	}

}
