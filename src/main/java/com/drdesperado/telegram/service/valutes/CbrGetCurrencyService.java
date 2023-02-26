package com.drdesperado.telegram.service.valutes;

import com.drdesperado.telegram.entity.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CbrGetCurrencyService {
    BigDecimal getCurrency(LocalDate date, Entity entity);
}
