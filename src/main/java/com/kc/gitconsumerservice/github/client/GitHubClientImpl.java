package com.kc.gitconsumerservice.github.client;

import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.exceptions.BadRequestException;
import com.kc.gitconsumerservice.exceptions.RateLimitException;
import com.kc.gitconsumerservice.exceptions.UnHandledException;
import com.kc.gitconsumerservice.exceptions.UserNotFoundException;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import com.kc.gitconsumerservice.utils.AppConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Locale;

@Log4j2
@Service
@Qualifier("gitHubService")
public class GitHubClientImpl implements GitHubClient {

    private final static String GITHUB_API_PER_PAGE = "per_page";
    private static final String GITHUB_API_PAGE = "page";

    private final WebClient webClient;

    private final MessageSource messageSource;

    @Value("${api.git-hub.path.repos}")
    private String reposUrlPath;

    @Value("${api.git-hub.path.branches}")
    private String branchesUrlPath;

    public GitHubClientImpl(WebClient webClient, MessageSource messageSource) {
        this.webClient = webClient;
        this.messageSource = messageSource;
    }

    @Override
    public Flux<GitRepo> getAllRepo(RepoRequest request) {
        Flux<GitRepo> gitRepos = webClient.get()
                .uri(uriBuilder -> uriBuilder.path(reposUrlPath)
                        .queryParam(GITHUB_API_PAGE, request.getPage())
                        .queryParam(GITHUB_API_PER_PAGE, request.getSize())
                        .build(request.getUsername()))
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> handleErrors(clientResponse))
                .bodyToFlux(GitRepo.class);
        return gitRepos;
    }

    @Override
    public Flux<GitBranch> getRepoBranches(String username, String repoName) {
        Flux<GitBranch> gitBranches = webClient.get()
                .uri(branchesUrlPath, username, repoName)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> handleErrors(clientResponse))
                .bodyToFlux(GitBranch.class);
        return gitBranches;
    }

    private Mono<Throwable> handleErrors(ClientResponse response) {
        log.debug(MessageFormat.format("GitHub API Error - ErrorCode {0}", response.rawStatusCode()));
        switch (response.rawStatusCode()) {
            case 400:
                return Mono.error(new BadRequestException(messageSource.getMessage("api.error.bad.request", null, Locale.ENGLISH)));
            case 403:
                return Mono.error(new RateLimitException(messageSource.getMessage("api.error.rate.limit", null, Locale.ENGLISH)));
            case 404:
                return Mono.error(new UserNotFoundException(messageSource.getMessage("api.error.user.not.found", null, Locale.ENGLISH)));
            default:
                return Mono.error(new UnHandledException(messageSource.getMessage("api.error.unhandled", null, Locale.ENGLISH)));
        }
    }
}
