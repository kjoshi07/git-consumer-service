package com.kc.gitconsumerservice.github;

import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.github.client.GitHubClientImpl;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitCommit;
import com.kc.gitconsumerservice.github.dto.GitOwner;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import com.kc.gitconsumerservice.github.mapper.GitSchemaMapper;
import com.kc.gitconsumerservice.utils.TestDataConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static com.kc.gitconsumerservice.utils.TestData.*;
import static com.kc.gitconsumerservice.utils.TestDataConstants.*;
import static org.mockito.ArgumentMatchers.any;

@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@Import({GitSchemaMapper.class})
public class GitHubServiceImplTest {

    @Value("${GIT_HUB_USER}")
    String gitHubUser;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @Mock
    private GitHubClientImpl gitHubClient;

    @Mock
    private GitSchemaMapper gitSchemaMapper;

    @Test
    void testGetNonForkedRepo() throws Exception {
        Flux<GitRepo> repositoryFlux = createMockRepositories();
        Flux<GitBranch> branchFlux = createMockBranches();
        Mockito.when(gitHubClient.getAllRepo(any())).thenReturn(repositoryFlux);
        Mockito.when(gitHubClient.getRepoBranches(any(), any())).thenReturn(branchFlux);
        Mockito.when(gitSchemaMapper.mapGitBranch(createTestGitBranch1())).thenReturn(createTestBranch1());
        Mockito.when(gitSchemaMapper.mapGitBranch(createTestGitBranch2())).thenReturn(createTestBranch2());
        Mockito.when(gitSchemaMapper.mapGitRepo(createTestGitRepo1(), mapBranches()))
                .thenReturn(createTestRepository1());
        Mockito.when(gitSchemaMapper.mapGitRepo(createTestGitRepo2(), mapBranches()))
                .thenReturn(createTestRepository2());
        Flux<Repository> result = gitHubService.getNonForkedRepos(RepoRequest.builder().username(gitHubUser).page(1).size(10).build());
        StepVerifier.create(result)
                .expectNextMatches(repository ->
                        repository.getName().equals(TestDataConstants.REPO_2) &&
                                repository.getOwnerLogin().equals(OWNER_2)
                                && repository.getBranches().get(0).getName().equals(BRANCH_2)
                                && repository.getBranches().get(0).getSha().equals(SHA_2)
                )
                .expectComplete()
                .verify();


    }

    private Flux<GitRepo> createMockRepositories() {
        return Flux.fromIterable(List.of(createTestGitRepo1(),
               createTestGitRepo2()));
    }

    private List<Branch> mapBranches() {
        return List.of(createTestBranch1(), createTestBranch2());
    }

    private Flux<GitBranch> createMockBranches() {
        return Flux.fromIterable(List.of(createTestGitBranch1(), createTestGitBranch2()));
    }

}
