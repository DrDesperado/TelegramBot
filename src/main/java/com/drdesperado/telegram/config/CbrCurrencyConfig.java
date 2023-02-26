package com.drdesperado.telegram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "currency.client")
@Data
@Component
public class CbrCurrencyConfig {
    private String url;
}
