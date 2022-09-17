package com.kc.gitconsumerservice.github.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GitRepo {

    private String name;
    private GitOwner owner;
    private List<GitBranch> branches;
    private boolean fork;
    private int size;

}
