package com.kc.gitconsumerservice.github.mapper;

import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GitSchemaMapper {

    public Branch mapGitBranch(GitBranch gitBranch) {
        return Branch.builder()
                .name(gitBranch.getName())
                .sha(gitBranch.getCommit().getSha())
                .build();
    }

    public Repository mapGitRepo(GitRepo gitRepo, List<Branch> branches) {
        return Repository.builder()
                .name(gitRepo.getName())
                .ownerLogin(gitRepo.getOwner().getLogin())
                .branches(branches)
                .build();
    }
}
