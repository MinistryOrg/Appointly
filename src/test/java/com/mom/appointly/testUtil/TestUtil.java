package com.mom.appointly.testUtil;

import com.mom.appointly.auth.model.AuthenticationRequest;
import com.mom.appointly.auth.model.AuthenticationResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtil {
    public String getToken(String userEmail, String userPassword, TestRestTemplate restTemplate){
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(userEmail, userPassword);
        ResponseEntity<AuthenticationResponse> authResponseEntity = restTemplate.postForEntity("/api/v1/auth/appointly/authenticate", authenticationRequest, AuthenticationResponse.class);
        assertEquals(HttpStatus.OK, authResponseEntity.getStatusCode());
        assertNotNull(authResponseEntity.getBody());
        assertNotNull(authResponseEntity.getBody().getToken());
        return authResponseEntity.getBody().getToken();
    }
}
