package com.drdesperado.telegram.client;

import com.drdesperado.telegram.config.CbrCurrencyConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class CbrRequestByDate implements HttpCurrencyDate {

    public final static String DATE_PATTERN = "dd/MM/yyyy";

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final CbrCurrencyConfig config;
    @Override
    public String requestByDate(LocalDate date) {
        String baseUrl = config.getUrl();
        HttpClient httpClient = HttpClient.newHttpClient();
        String uri = buildUri(baseUrl, date);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildUri(String baseUrl, LocalDate date) {
        return UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParam("date_req", DATE_TIME_FORMATTER.format(date))
                .build()
                .toUriString();
    }
}
