package com.kc.gitconsumerservice.github;

import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import com.kc.gitconsumerservice.github.client.GitHubClient;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import com.kc.gitconsumerservice.github.mapper.GitSchemaMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

@Log4j2
@Service
public class GitHubServiceImpl implements GitHubService {

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private GitSchemaMapper mapper;

    @RateLimiter(name = "gitRepoService")
    @Cacheable(value="git-repos")
    @Override
    public Flux<Repository> getNonForkedRepos(RepoRequest request) {
        log.debug(MessageFormat.format("Fetch all Git-Repos from github.com for user {0}", request.getUsername()));

        return gitHubClient.getAllRepo(request)
                .filter(gitRepo -> !gitRepo.isFork())
                .flatMap(getBranchesByRepo(request.getUsername()));
    }

    private Function<GitRepo, Publisher<? extends Repository>> getBranchesByRepo(String userName) {
          return repo -> Mono.zip(
                  Mono.just(repo), gitHubClient.getRepoBranches(userName,repo.getName())
                          .map(mapper::mapGitBranch).collectList()
          ).map(mapRepository());
    }

    private Function<Tuple2<GitRepo, List<Branch>>, Repository> mapRepository() {
        return tuple -> {
            GitRepo gitRepo = tuple.getT1();
            List<Branch> branches = tuple.getT2();
            return mapper.mapGitRepo(gitRepo, branches);
        };
    }
}
