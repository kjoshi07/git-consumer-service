package com.kc.gitconsumerservice.consumer.controller;

import com.kc.gitconsumerservice.consumer.dto.ErrorResponse;
import com.kc.gitconsumerservice.consumer.dto.RepoRequest;
import com.kc.gitconsumerservice.consumer.dto.Repository;
import com.kc.gitconsumerservice.consumer.services.GitHubService;
import com.kc.gitconsumerservice.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.text.MessageFormat;

@RestController
@RequestMapping(value = "/api/v1/git-repo")
@Log4j2
public class GitHubConsumerController {

    @Autowired
    private GitHubService gitHubService;

    @Operation(
            summary = "Get All Non-Forked Repository for a User",
            parameters = {@Parameter(name = "page", in = ParameterIn.QUERY, required = false, description = "Page Number"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, required = false, description = "Total Records")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully Returned user's repository",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Repository.class)))}),
            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit exceeded",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404",
                    description = "User not found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @GetMapping(value = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Repository> getAllUserRepos(@PathVariable("username") String username,
                                            @RequestParam(value = "page", defaultValue = AppConstants.GITHUB_API_PAGE_DEFAULT) Integer page,
                                            @RequestParam(value = "size", defaultValue = AppConstants.GIT_API_PER_PAGE_DEFAULT) Integer size) {
        log.debug(MessageFormat.format("GET git repo request for user {0} with page {1} & size {2}", username, page, size));
        RepoRequest request = RepoRequest.builder()
                .username(username)
                .page(page)
                .size(size)
                .build();
        Flux<Repository> repos = gitHubService.getNonForkedRepos(request);

        log.debug(MessageFormat.format("Non-Forked repository successfully returned for user {0}", username));

        return repos;

    }

}
