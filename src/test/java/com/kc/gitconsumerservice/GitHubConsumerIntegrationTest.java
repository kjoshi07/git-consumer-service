package com.kc.gitconsumerservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kc.gitconsumerservice.consumer.dto.ErrorResponse;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import java.util.List;

import static com.kc.gitconsumerservice.utils.TestData.createTestRepository1;
import static com.kc.gitconsumerservice.utils.TestData.createTestRepository2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@MockServerTest("server.url=http://localhost:9001")
@TestPropertySource(locations = "/test.properties")
@SpringBootTest(classes = GitConsumerServiceApplication.class)
public class GitHubConsumerIntegrationTest {


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

    private MockServerClient mockServerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setBasicAuth() {
        basicAuthHeader = "basic " + Base64Utils.encodeToString((testUser + ":" + testPassword).getBytes());
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + this.mockServerClient.getPort())
                .build();
    }


    @Disabled
    @Test
    void whenNoCredentials_UnAuthorize_401() {
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private void mockService(int statusCode, String responseData) {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath(gitHubUserApiPath)
                )
                .respond(
                        response()
                                .withStatusCode(statusCode)
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withBody(responseData)
                );
    }

    @Test
    void testGetAllUserRepos_Success_200() throws Exception {
        List<Repository> expected = List.of(createTestRepository1(), createTestRepository2());
        String responseData = objectMapper.writeValueAsString(expected);
        mockService(HttpStatus.OK.value(), responseData);

        webTestClient
                .get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Repository.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Repository> result = listEntityExchangeResult.getResponseBody();
                    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
                });
    }

    @Test
    void testGetAllUserRepos_Pagination_Success_200() throws Exception {
        List<Repository> expected = List.of(createTestRepository1(), createTestRepository2());
        String responseData = objectMapper.writeValueAsString(expected);
        mockService(HttpStatus.OK.value(), responseData);
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
                .expectBodyList(Repository.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Repository> result = listEntityExchangeResult.getResponseBody();
                    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
                });
    }

    @Test
    void testGetAllUserRepos_ContentTypeXml_Failed_406() throws Exception {
        String errorMessage = "406 NOT_ACCEPTABLE \"Could not find acceptable representation\"";
        ErrorResponse expected = ErrorResponse.builder()
                .status(HttpStatus.NOT_ACCEPTABLE.value())
                .message(errorMessage)
                .build();

        String responseData = objectMapper.writeValueAsString(expected);
        mockService(HttpStatus.NOT_ACCEPTABLE.value(), responseData);
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_ACCEPTABLE.value())
                .jsonPath("$.message").isEqualTo(errorMessage);
    }

}
