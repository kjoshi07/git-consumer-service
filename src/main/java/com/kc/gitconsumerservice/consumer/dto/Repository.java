package com.kc.gitconsumerservice.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Repository {

    private String name;
    private String ownerLogin;
    private List<Branch> branches;
}
