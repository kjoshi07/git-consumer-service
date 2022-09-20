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
        Mockito.when(gitSchemaMapper.mapGitBranch(createBranch1())).thenReturn(mapBranch(createBranch1()));
        Mockito.when(gitSchemaMapper.mapGitBranch(createBranch2())).thenReturn(mapBranch(createBranch2()));
        Mockito.when(gitSchemaMapper.mapGitRepo(createRepo1(), mapBranches()))
                .thenReturn(mapRepo(createRepo1(), List.of(mapBranch(createBranch1()), mapBranch(createBranch2()))));
        Mockito.when(gitSchemaMapper.mapGitRepo(createRepo2(), mapBranches()))
                .thenReturn(mapRepo(createRepo2(), List.of(mapBranch(createBranch1()), mapBranch(createBranch2()))));
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
        return Flux.fromIterable(List.of(createRepo1(),
               createRepo2()));
    }

    private Repository mapRepo(GitRepo gitRepo, List<Branch> branches) {
        return Repository.builder()
                .name(gitRepo.getName())
                .ownerLogin(gitRepo.getOwner().getLogin())
                .branches(branches)
                .build();
    }

    private Repository mapRepo2(GitRepo gitRepo, List<Branch> branches) {
        return Repository.builder()
                .name(gitRepo.getName())
                .ownerLogin(gitRepo.getOwner().getLogin())
                .branches(branches)
                .build();
    }

    private List<Branch> mapBranches() {
        return List.of(mapBranch(createBranch1()), mapBranch(createBranch2()));
    }

    public Branch mapBranch(GitBranch gitBranch) {
        return Branch.builder()
                .name(gitBranch.getName())
                .sha(gitBranch.getCommit().getSha())
                .build();
    }

    private GitRepo createRepo1() {
        return GitRepo.builder().name(REPO_1)
                .owner(GitOwner.builder().login(OWNER_1).build())
                .fork(true)
                .branches(null).build();
    }

    private GitRepo createRepo2() {
        return GitRepo.builder().name(REPO_2)
                .owner(GitOwner.builder().login(OWNER_2).build())
                .fork(false)
                .branches(null)
                .build();
    }

    private Flux<GitBranch> createMockBranches() {
        return Flux.fromIterable(List.of(createBranch1(), createBranch2()));
    }

    private GitBranch createBranch1() {
        return GitBranch.builder().name(BRANCH_1)
                .commit(GitCommit.builder().sha(SHA_1).build())
                .build();
    }
    private GitBranch createBranch2() {
        return GitBranch.builder().name(BRANCH_2)
                .commit(GitCommit.builder().sha(SHA_2).build())
                .build();
    }
}
