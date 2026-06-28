package com.serjnn.ClientService;

import com.serjnn.ClientService.dto.AuthRequest;
import com.serjnn.ClientService.dto.ClientInfoDto;
import com.serjnn.ClientService.dto.RegRequest;
import com.serjnn.ClientService.repository.ClientRepository;
import com.serjnn.ClientService.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Disabled by default because running Testcontainers requires a local Docker daemon (e.g. Docker Desktop) to be active.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "eureka.client.enabled=false",
        "spring.sql.init.mode=always"
})
public class ClientServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        clientRepository.deleteAll().block();
    }

    @Test
    public void testRegisterAndAuthenticate() {
        RegRequest reg = new RegRequest();
        reg.setMail("test@example.com");
        reg.setPassword("secret123");

        webTestClient.post()
                .uri("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reg)
                .exchange()
                .expectStatus().isOk();

        AuthRequest auth = new AuthRequest();
        auth.setMail("test@example.com");
        auth.setPassword("secret123");

        webTestClient.post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(auth)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(token -> {
                    assertThat(token).isNotEmpty();
                });
    }
}
