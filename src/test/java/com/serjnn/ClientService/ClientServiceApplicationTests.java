package com.serjnn.ClientService;

import com.serjnn.ClientService.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled by default because running Testcontainers requires a local Docker daemon (e.g. Docker Desktop) to be active.")
@SpringBootTest
class ClientServiceApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
