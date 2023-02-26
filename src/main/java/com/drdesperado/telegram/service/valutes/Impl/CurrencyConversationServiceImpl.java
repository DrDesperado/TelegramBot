package com.drdesperado.telegram.service.valutes.Impl;

import com.drdesperado.telegram.service.valutes.CurrencyConversationService;
import com.drdesperado.telegram.entity.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;




@Component
@AllArgsConstructor
public class CurrencyConversationServiceImpl implements CurrencyConversationService {

    private final CbrGetCurrencyServiceImpl getCurrencyService;

    @Override
    public BigDecimal getConvertingRatio(Entity original, Entity target) {
        if (target == Entity.RUB && original == Entity.RUB) {return BigDecimal.valueOf(1L);}

        else if (target == Entity.RUB) {return getCurrencyService.getCurrency(LocalDate.now(), original);}

        else if (original == Entity.RUB) {return BigDecimal.valueOf(1L)
                .divide(getCurrencyService.getCurrency(LocalDate.now(), target), 5, RoundingMode.HALF_UP);}

        else {
            BigDecimal originalRate = getCurrencyService.getCurrency(LocalDate.now(), original);
            BigDecimal targetRate = getCurrencyService.getCurrency(LocalDate.now(), target);
            return originalRate.divide(targetRate, RoundingMode.HALF_UP);
        }
    }
}