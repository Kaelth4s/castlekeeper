package org.kaelth4s.castlekeeper.bot.handler;

import org.kaelth4s.castlekeeper.bot.keyboard.KeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class StartHandler implements CommandHandler {

    @Override
    public SendMessage handle(Long chatId, String text) {
        String msg = "\uD83C\uDFF0 Ты стоишь у массивных ворот замка CastleKeeper.\n\n" +
                "Над вратами высечена надпись: «Здесь хранятся истории всех твердынь мира».\n\n" +
                "Я — привратник. Внутри тебя ждут свитки с летописями замков со всего света.\n\n" +
                "Решай, путник: хочешь войти в главный зал и получить полный доступ? Или просто взглянуть на список замков?";

        SendMessage sm = new SendMessage(chatId.toString(), msg);
        sm.setReplyMarkup(KeyboardFactory.replyGrid(List.of(
                "\uD83D\uDEAA Войти в замок",
                "\uD83D\uDCDC Список замков"
        ), 2));
        return sm;
    }
}
