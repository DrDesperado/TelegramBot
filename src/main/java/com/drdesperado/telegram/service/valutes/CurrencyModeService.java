package com.drdesperado.telegram.service.valutes;

import com.drdesperado.telegram.entity.Entity;

public interface CurrencyModeService {
    Entity getOriginalCurrency(long chatId);
    Entity getTargetCurrency(long chatId);
    void setOriginalCurrency(long chatId, Entity entity);
    void setTargetCurrency(long chatId, Entity entity);
}
