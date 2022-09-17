package com.kc.gitconsumerservice.consumer.services;

import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import reactor.core.publisher.Flux;

public interface GitHubService {

    Flux<Repository> getNonForkedRepos(RepoRequest request);
}
