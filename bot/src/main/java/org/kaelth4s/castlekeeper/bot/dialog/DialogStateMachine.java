package org.kaelth4s.castlekeeper.bot.dialog;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
public class DialogStateMachine {
    private final DialogStateRepository repo;

    public DialogStateMachine(DialogStateRepository repo) {
        this.repo = repo;
    }

    public void setPrefix(Long chatId, String prefix) { repo.putPrefix(chatId, prefix); }
    public String getPrefix(Long chatId) { return repo.getPrefix(chatId).orElse("menu"); }

    public void start(Long chatId, DialogStep step) { repo.putStep(chatId, step); }

    public void cancel(Long chatId) { repo.removeStep(chatId); }

    public void cancelAll(Long chatId) { repo.removeStep(chatId); repo.removePrefix(chatId); }

    public SendMessage next(Long chatId, String input) {
        DialogStep step = repo.getStep(chatId).orElse(null);
        if (step == null) return null;
        repo.removeStep(chatId);
        return step.process(chatId, input);
    }

    public static SendMessage removeKeyboard(SendMessage msg) {
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        return msg;
    }
}
