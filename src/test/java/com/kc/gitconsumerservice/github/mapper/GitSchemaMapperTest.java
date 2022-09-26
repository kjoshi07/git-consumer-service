package com.kc.gitconsumerservice.github.mapper;

import com.kc.gitconsumerservice.consumer.dto.Branch;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import com.kc.gitconsumerservice.github.client.GitHubClient;
import com.kc.gitconsumerservice.github.dto.GitBranch;
import com.kc.gitconsumerservice.github.dto.GitCommit;
import com.kc.gitconsumerservice.github.dto.GitOwner;
import com.kc.gitconsumerservice.github.dto.GitRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.kc.gitconsumerservice.utils.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GitSchemaMapperTest {

    @Mock
    private GitSchemaMapper mapper;

    @Test
    void mapGitBranchTest_returnBranch() throws Exception {
        Branch branch = createTestBranch1();
        Mockito.when(mapper.mapGitBranch(any())).thenReturn(branch);
        Branch result = mapper.mapGitBranch(createTestGitBranch1());
        assertThat(result).usingRecursiveComparison().isEqualTo(branch);

    }

    @Test
    void mapGitRepoTest_returnRepository() throws Exception {
        Repository repository = createTestRepository1();
        Mockito.when(mapper.mapGitRepo(any(), any())).thenReturn(repository);
        Repository result = mapper.mapGitRepo(createTestGitRepo1(), List.of(createTestBranch1()));
        assertThat(result).usingRecursiveComparison().isEqualTo(repository);

    }




}
