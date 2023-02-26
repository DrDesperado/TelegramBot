package com.drdesperado.telegram.service.valutes.Impl;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

import com.drdesperado.telegram.entity.Entity;
import com.drdesperado.telegram.service.valutes.CurrencyModeService;


@Component
public class CurrencyModeServiceImpl implements CurrencyModeService {
    private final Map<Long, Entity> originalCurrency = new HashMap<>();
    private final Map<Long, Entity> targetCurrency = new HashMap<>();



    @Override
    public Entity getOriginalCurrency(long chatId) {
        return originalCurrency.getOrDefault(chatId, Entity.USD);
    }

    @Override
    public Entity getTargetCurrency(long chatId) {
        return targetCurrency.getOrDefault(chatId, Entity.RUB);
    }

    @Override
    public void setOriginalCurrency(long chatId, Entity entity) {
        originalCurrency.put(chatId, entity);
    }

    @Override
    public void setTargetCurrency(long chatId, Entity entity) {
        targetCurrency.put(chatId, entity);
    }
}
