package com.drdesperado.telegram.service.valutes.Impl;

import com.drdesperado.telegram.client.HttpCurrencyDate;
import com.drdesperado.telegram.entity.Entity;
import com.drdesperado.telegram.schema.ValCurs;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toMap;

@Service
public class CbrGetCurrencyServiceImpl implements com.drdesperado.telegram.service.valutes.CbrGetCurrencyService {
    private final Cache<LocalDate, Map<Entity, BigDecimal>> cache;

    private final HttpCurrencyDate client;

    public CbrGetCurrencyServiceImpl(HttpCurrencyDate client) {
        this.cache = CacheBuilder.newBuilder().build();
        this.client = client;
    }



    @Override
    public BigDecimal getCurrency(LocalDate date, Entity entity) {
        try {
            return cache.get(date, this::requestForCurrencyDate).get(entity);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<Entity, BigDecimal> requestForCurrencyDate() {
        String xml = client.requestByDate(LocalDate.now());
        ValCurs valCurs = xmlToObject(xml);

        return valCurs.getValute().stream()
                .collect(
                        toMap(item -> Entity.valueOf(item.getCharCode()), this::parseDecimal)
                );
    }

    private BigDecimal parseDecimal(ValCurs.Valute item) {
        long nominal = item.getNominal();
        BigDecimal value = new BigDecimal(item.getValue().replace(",", "."));
        return value.divide(BigDecimal.valueOf(nominal), 5, RoundingMode.HALF_UP);
    }

    private ValCurs xmlToObject(String xml) {
        try(StringReader reader = new StringReader(xml)) {
            JAXBContext context = JAXBContext.newInstance(ValCurs.class);
            return (ValCurs) context.createUnmarshaller().unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
