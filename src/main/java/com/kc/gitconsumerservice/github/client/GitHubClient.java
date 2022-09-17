package com.kc.gitconsumerservice.github.client;

import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface GitHubClient {

    Flux<GitRepo> getAllRepo(RepoRequest request);

    Flux<GitBranch> getRepoBranches(String username, String repoName);

}
