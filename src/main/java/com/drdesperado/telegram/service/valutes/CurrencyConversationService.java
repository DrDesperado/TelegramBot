package com.drdesperado.telegram.service.valutes;

import com.drdesperado.telegram.entity.Entity;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

public interface CurrencyConversationService {
    BigDecimal getConvertingRatio(Entity original, Entity target) throws IOException, ParserConfigurationException, SAXException, ExecutionException;
}
