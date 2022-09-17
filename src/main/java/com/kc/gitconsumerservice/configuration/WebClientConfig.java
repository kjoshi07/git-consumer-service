package com.kc.gitconsumerservice.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${api.git-hub.url}")
    private String baseURL;

    @Value("${api.git-hub.auth-token}")
    private String authToken;

    private final String AUTHORIZATION_PREFIX = "Bearer ";
    private final String GITHUB_CONTENT_TYPE = "application/vnd.github+json";


    @Bean
    public WebClient getWebClient() {
        HttpClient httpClient = HttpClient.create();
        httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10))
                        .addHandlerLast(new WriteTimeoutHandler(10)));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        String authorizationToken = String.format("%s %s", AUTHORIZATION_PREFIX, authToken);

        return WebClient.builder()
                .baseUrl(baseURL)
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, GITHUB_CONTENT_TYPE)
                .build();
    }
}
