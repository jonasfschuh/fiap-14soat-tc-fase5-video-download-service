package br.com.fiap.application.unit.controllers;

import br.com.fiap.application.adapters.AuthProxyController;
import br.com.fiap.application.dtos.AuthLoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@DisplayName("AuthProxyController - Unit Tests")
class AuthProxyControllerTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private AuthProxyController controller;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        controller = new AuthProxyController(restTemplate);
    }

    @Test void login_withoutUrl_returnsServiceUnavailable() {
        ResponseEntity<Object> response = controller.login(new AuthLoginRequest("admin", "pass"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat((Map<String, Object>) response.getBody()).containsKey("error");
    }

    @Test void login_withUrl_success_returnsOk() {
        ReflectionTestUtils.setField(controller, "authServiceUrl", "http://auth.local");
        mockServer.expect(requestTo("http://auth.local/login"))
                .andRespond(withSuccess("{\"token\":\"jwt\"}", MediaType.APPLICATION_JSON));
        assertThat(controller.login(new AuthLoginRequest("admin","pass")).getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test void login_with401_returnsUnauthorized() {
        ReflectionTestUtils.setField(controller, "authServiceUrl", "http://auth.local");
        mockServer.expect(requestTo("http://auth.local/login")).andRespond(withUnauthorizedRequest());
        assertThat(controller.login(new AuthLoginRequest("u","w")).getStatusCode().value()).isEqualTo(401);
        mockServer.verify();
    }

    @Test void login_withConnectionFailure_returns500() {
        ReflectionTestUtils.setField(controller, "authServiceUrl", "http://auth.local");
        mockServer.expect(requestTo("http://auth.local/login"))
                .andRespond(withException(new IOException("refused")));
        assertThat(controller.login(new AuthLoginRequest("a","b")).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        mockServer.verify();
    }

    @Test void authLoginRequest_gettersSetters() {
        AuthLoginRequest r = new AuthLoginRequest();
        r.setUsername("u"); r.setPassword("p");
        assertThat(r.getUsername()).isEqualTo("u");
        assertThat(r.getPassword()).isEqualTo("p");
    }

    @Test void authLoginRequest_constructor() {
        AuthLoginRequest r = new AuthLoginRequest("admin", "secret");
        assertThat(r.getUsername()).isEqualTo("admin");
    }
}
