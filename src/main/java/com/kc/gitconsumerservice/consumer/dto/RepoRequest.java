package com.kc.gitconsumerservice.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RepoRequest {
    private String username;
    private int page;
    private int size;
}
