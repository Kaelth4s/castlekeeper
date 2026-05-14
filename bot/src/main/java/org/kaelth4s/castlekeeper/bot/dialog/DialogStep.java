package org.kaelth4s.castlekeeper.bot.dialog;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@FunctionalInterface
public interface DialogStep {
    SendMessage process(Long chatId, String input);
}
