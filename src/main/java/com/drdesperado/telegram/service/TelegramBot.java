package com.drdesperado.telegram.service;

import com.drdesperado.telegram.config.BotConfig;
import com.drdesperado.telegram.entity.Entity;
import com.drdesperado.telegram.service.valutes.CurrencyConversationService;
import com.drdesperado.telegram.service.valutes.CurrencyModeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Autowired
    private final CurrencyModeService currencyModeService;
    @Autowired
    private final CurrencyConversationService currencyConversationService;


    public TelegramBot(BotConfig botConfig, CurrencyModeService currencyModeService, CurrencyConversationService currencyConversationService) {
        this.botConfig = botConfig;
        this.currencyModeService = currencyModeService;
        this.currencyConversationService = currencyConversationService;

    }


    @Override
    public String getBotUsername() {
        return this.botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return this.botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            try {
                handleMessage(update.getMessage());
            } catch (TelegramApiException | IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else if (update.hasCallbackQuery()) {
            try {
                handleCallback(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleCallback(CallbackQuery callbackQuery) throws TelegramApiException {
        Message message = callbackQuery.getMessage();
        long ChatId = message.getChatId();
        String[] data = callbackQuery.getData().split(":");
        String action = data[0];
        Entity newCurrent = Entity.valueOf(data[1]);

        switch (action) {
            case "ORIGINAL": {
                currencyModeService.setOriginalCurrency(ChatId, newCurrent);
                break;
            }
            case "TARGET": {
                currencyModeService.setTargetCurrency(ChatId, newCurrent);
                break;
            }
        }
        var newButtons = setButtons(ChatId);
        execute(EditMessageReplyMarkup.builder().chatId(ChatId).messageId(message.getMessageId()).replyMarkup(InlineKeyboardMarkup.builder().keyboard(newButtons).build()).build());

    }

    private void handleMessage(Message message) throws TelegramApiException, IOException, ParserConfigurationException, SAXException, ExecutionException {
        long ChatId = message.getChatId();
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());

                switch (command) {
                    case "/start": {
                        startCommandReceived(ChatId, message.getChat().getFirstName());
                        break;
                    }

                    case "/set_currency": {
                        var buttons = setButtons(ChatId);
                        sendMessage(ChatId, "Please choose Original and Target currencies", buttons);
                        break;

                    }
                }

            }
        }

        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Entity originalCurrency = currencyModeService.getOriginalCurrency(ChatId);
            Entity targetCurrency = currencyModeService.getTargetCurrency(ChatId);
            BigDecimal ratio = currencyConversationService.getConvertingRatio(originalCurrency, targetCurrency);


            if (value.isPresent()) {
                String answer = String.format("%4.2f %s is %4.2f %s", value.get(), originalCurrency, (ratio.multiply(BigDecimal.valueOf(value.get()))), targetCurrency);
                //String answer = ratio.toString();
                sendMessage(ChatId, answer);
            }
        }

    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    private void startCommandReceived(long id, String name) throws TelegramApiException {
        String answer = "Hi, " + name + ", welcome to currency conversation!";
        sendMessage(id, answer);

    }

    private void sendMessage(long id, String answer) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(id);
        sendMessage.setText(answer);
        execute(sendMessage);
    }

    private void sendMessage(long id, String answer, List<List<InlineKeyboardButton>> buttons) throws TelegramApiException {
        execute(SendMessage.builder().text(answer).chatId(id).replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build()).build());
    }

    private String getCurrencyButton(Entity saved, Entity current) {
        return saved == current ? current + " ✅" : current.name();
    }

    private List<List<InlineKeyboardButton>> setButtons(Long ChatId) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Entity originalCurrency = currencyModeService.getOriginalCurrency(ChatId);
        Entity targetCurrency = currencyModeService.getTargetCurrency(ChatId);
        for (Entity currency : Entity.values()) {
            buttons.add(Arrays.asList(InlineKeyboardButton.builder().text(getCurrencyButton(originalCurrency, currency)).callbackData("ORIGINAL:" + currency).build(), InlineKeyboardButton.builder().text(getCurrencyButton(targetCurrency, currency)).callbackData("TARGET:" + currency).build()));
        }
        return buttons;
    }

}
