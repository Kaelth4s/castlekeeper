package org.kaelth4s.castlekeeper.bot;

import org.kaelth4s.castlekeeper.bot.dispatcher.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class CastleKeeperBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(CastleKeeperBot.class);
    private final CommandDispatcher dispatcher;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public CastleKeeperBot(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getBotToken() { return botToken; }
    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            log.info("Message from {}: {}", chatId, text);
            SendMessage reply = dispatcher.dispatch(chatId, text);
            if (reply != null) executeMsg(reply);
        }
        else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer msgId = update.getCallbackQuery().getMessage().getMessageId();
            log.info("Callback from {}: {}", chatId, data);
            SendMessage reply = dispatcher.handleCallback(chatId, data);
            acknowledgeCallback(update.getCallbackQuery().getId());
            boolean keepKeyboard = "noop".equals(data) || "ignore".equals(data);
            if (!keepKeyboard) removeInlineKeyboard(chatId, msgId);
            if (reply != null) executeMsg(reply);
        }
    }

    private void removeInlineKeyboard(Long chatId, Integer messageId) {
        try {
            EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .replyMarkup(new InlineKeyboardMarkup(List.of()))
                    .build();
            super.execute(edit);
        } catch (TelegramApiException e) {
            // Silently ignore — message may have no keyboard or be too old
        }
    }

    private void executeMsg(SendMessage message) {
        try {
            super.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private void acknowledgeCallback(String callbackId) {
        try {
            super.execute(new AnswerCallbackQuery(callbackId));
        } catch (TelegramApiException e) {
            log.error("Failed to acknowledge callback", e);
        }
    }
}
