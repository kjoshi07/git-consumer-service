package com.kc.gitconsumerservice.controller;

import com.kc.gitconsumerservice.configuration.ResourceWebPropertiesConfig;
import com.kc.gitconsumerservice.configuration.SecurityConfig;
import com.kc.gitconsumerservice.consumer.controller.GitHubConsumerController;
import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import com.kc.gitconsumerservice.exceptions.BadRequestException;
import com.kc.gitconsumerservice.exceptions.RateLimitException;
import com.kc.gitconsumerservice.exceptions.UnHandledException;
import com.kc.gitconsumerservice.exceptions.UserNotFoundException;
import com.kc.gitconsumerservice.github.GitHubServiceImpl;
import com.kc.gitconsumerservice.utils.TestDataConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = GitHubConsumerController.class)
@Import({GitHubServiceImpl.class, ResourceWebPropertiesConfig.class, SecurityConfig.class})
@ComponentScan({"com.kc.gitconsumerservice.exceptions"})
public class GitHubConsumerControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GitHubService gitHubService;

    @Value("${CONSUMER_GET_API_USER_PATH}")
    private String gitHubUserApiPath;

    @Value(("${CONSUMER_GET_API_NON_USER_PATH}"))
    private String githubNonUserApiPath;

    @Value("${TEST_USER}")
    private String testUser;

    @Value("${TEST_PASSWORD}")
    private String testPassword;

    private String basicAuthHeader;

    @BeforeEach
    void setBasicAuth() {
        basicAuthHeader =  "basic " + Base64Utils.encodeToString((testUser + ":" + testPassword).getBytes());
    }

    private Repository mockGitRepository() {
        Branch branch = Branch.builder().name(TestDataConstants.BRANCH_1).sha(TestDataConstants.SHA_1).build();
        return Repository.builder()
                .name(TestDataConstants.REPO_1)
                .ownerLogin(TestDataConstants.OWNER_1)
                .branches(List.of(branch)).build();
    }


    @Test
    void testGetAllUserRepos_Success_200() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenReturn(Flux.just(mockGitRepository()));
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
    void testGetAllUserRepos_Pagination_Success_200() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenReturn(Flux.just(mockGitRepository()));
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(gitHubUserApiPath)
                        .queryParam("page", 1)
                        .queryParam("size", 10)
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
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenThrow(new UnsupportedMediaTypeException("Content-Type not supported"));
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }


    @Test
    void testGetAllUserRepos_UserNotFound_Failed_404() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenThrow(new UserNotFoundException("User is not found in github!"));
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

    @Test
    public void testGetAllUserRepos_TooManyRequest_Failed_403() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenThrow(new RateLimitException("rate limit is exceeded to github!"));
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testGetAllUserRepos_BadRequest_Failed_400() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenThrow(new BadRequestException("Bad Request!"));
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void testGetAllUserRepos_UnHandled_Failed_500() throws Exception {
        Mockito.when(gitHubService.getNonForkedRepos(any())).thenThrow(new UnHandledException("UnHandled Error."));
        webTestClient.get()
                .uri(gitHubUserApiPath)
                .headers(headers -> {
                    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
                })
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
