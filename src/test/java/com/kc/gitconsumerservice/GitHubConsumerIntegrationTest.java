package com.kc.gitconsumerservice;

import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;


@TestPropertySource(locations = "/test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GitConsumerServiceApplication.class)
public class GitHubConsumerIntegrationTest {


    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GitHubService gitHubService;

    @Value("${CONSUMER_GET_API_USER_PATH}")
    private String gitHubUserApiPath;

    @Value(("${CONSUMER_GET_API_NON_USER_PATH}"))
    private String githubNonUserApiPath;

    @Value("${PAGE}")
    private String page;

    @Value("${SIZE}")
    private String size;

    @Value("${TEST_USER}")
    private String testUser;

    @Value("${TEST_PASSWORD}")
    private String testPassword;

    private String basicAuthHeader;

    @BeforeEach
    void setBasicAuth() {
       basicAuthHeader =  "basic " + Base64Utils.encodeToString((testUser + ":" + testPassword).getBytes());
    }


    @Test
    void whenNoCredentials_UnAuthorize_401() {
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testGetAllUserRepos_Success_200() {
       webTestClient
                .get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Repository.class);
    }

    @Test
    void testGetAllUserRepos_Pagination_Success_200() {
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(gitHubUserApiPath)
                        .queryParam(page, 1)
                        .queryParam(size, 10)
                        .build())
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Repository.class);
    }

    @Test
    void testGetAllUserRepos_ContentTypeXml_Failed_406() {
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }


    @Test
    void testGetAllUserRepos_UserNotFound_Failed_404() {
        webTestClient.get()
                .uri(githubNonUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
