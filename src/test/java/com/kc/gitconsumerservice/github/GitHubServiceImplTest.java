package com.kc.gitconsumerservice.github;

import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.github.client.GitHubClientImpl;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitCommit;
import com.kc.gitconsumerservice.github.dto.GitOwner;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import com.kc.gitconsumerservice.utils.TestDataConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.kc.gitconsumerservice.utils.TestDataConstants.*;
import static org.mockito.ArgumentMatchers.any;

@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
public class GitHubServiceImplTest {

    @Value("${GIT_HUB_USER}")
    String gitHubUser;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @Mock
    private GitHubClientImpl gitHubClient;

    @Test
    void testGetNonForkedRepo() throws Exception {
        Flux<GitRepo> repositoryFlux = createMockRepositories();
        Flux<GitBranch> branchFlux = createMockBranches();
        Mockito.when(gitHubClient.getAllRepo(any())).thenReturn(repositoryFlux);
        Mockito.when(gitHubClient.getRepoBranches(any(), any())).thenReturn(branchFlux);
        Flux<Repository> result = gitHubService.getNonForkedRepos(RepoRequest.builder().username(gitHubUser).page(1).size(10).build());
        StepVerifier.create(result)
                .expectNextMatches(repository ->
                        repository.getName().equals(TestDataConstants.REPO_2) &&
                                repository.getOwnerLogin().equals(OWNER_2)
                )
                .expectComplete()
                .verify();


    }

    private Flux<GitRepo> createMockRepositories() {
        return Flux.fromIterable(List.of(GitRepo.builder().name(REPO_1)
                        .owner(GitOwner.builder().login(OWNER_1).build())
                        .fork(true)
                        .branches(null).build(),
                GitRepo.builder().name(REPO_2)
                        .owner(GitOwner.builder().login(OWNER_2).build())
                        .fork(false)
                        .branches(null)
                        .build()));
    }

    private Flux<GitBranch> createMockBranches() {
        GitBranch branch1 = GitBranch.builder().name(BRANCH_1)
                .commit(GitCommit.builder().sha(SHA_1).build())
                .build();

        GitBranch branch2 = GitBranch.builder().name(BRANCH_2)
                .commit(GitCommit.builder().sha(SHA_2).build())
                .build();
        return Flux.fromIterable(List.of(branch1, branch2));
    }
}
