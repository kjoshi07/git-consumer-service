package com.kc.gitconsumerservice.utils;

import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitCommit;
import com.kc.gitconsumerservice.github.dto.GitOwner;
import com.kc.gitconsumerservice.github.dto.GitRepo;

import java.util.List;

import static com.kc.gitconsumerservice.utils.TestDataConstants.*;
import static com.kc.gitconsumerservice.utils.TestDataConstants.SHA_1;

public class TestData {



    public static Branch createTestBranch1() {
        return Branch.builder()
                .name(BRANCH_1)
                .sha(SHA_1).build();
    }

    public static Branch createTestBranch2() {
        return Branch.builder()
                .name(BRANCH_2)
                .sha(SHA_2).build();
    }


    public static GitBranch createTestGitBranch1() {
        return GitBranch.builder().name(BRANCH_1)
                .commit(GitCommit.builder().sha(SHA_1).build())
                .build();
    }

    public static GitBranch createTestGitBranch2() {
        return GitBranch.builder().name(BRANCH_2)
                .commit(GitCommit.builder().sha(SHA_2).build())
                .build();
    }

    public static Repository createTestRepository1() {
        return Repository.builder()
                .name(REPO_1)
                .ownerLogin(OWNER_1)
                .branches(List.of(createTestBranch1()))
                .build();
    }

    public static Repository createTestRepository2() {
        return Repository.builder()
                .name(REPO_2)
                .ownerLogin(OWNER_2)
                .branches(List.of(createTestBranch2()))
                .build();
    }

    public static GitRepo createTestGitRepo2() {
        return GitRepo.builder().name(REPO_2)
                .owner(GitOwner.builder().login(OWNER_2).build())
                .fork(false)
                .branches(null)
                .build();
    }

    public static GitRepo createTestGitRepo1() {
        return GitRepo.builder().name(REPO_1)
                .owner(GitOwner.builder().login(OWNER_1).build())
                .fork(true)
                .branches(null).build();
    }

}
