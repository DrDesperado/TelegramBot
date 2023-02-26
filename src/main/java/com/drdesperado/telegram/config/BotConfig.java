package com.drdesperado.telegram.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String name;

    private String token;
}
