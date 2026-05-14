package org.kaelth4s.castlekeeper.bot.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@FunctionalInterface
public interface CommandHandler {
    SendMessage handle(Long chatId, String text);
}
