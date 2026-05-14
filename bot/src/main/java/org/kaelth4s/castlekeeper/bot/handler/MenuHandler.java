package org.kaelth4s.castlekeeper.bot.handler;

import org.kaelth4s.castlekeeper.bot.keyboard.KeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class MenuHandler implements CommandHandler {

    @Override
    public SendMessage handle(Long chatId, String text) {
        String msg = "\uD83D\uDEE1\uFE0F Главный зал CastleKeeper.\n\n" +
                "Пять высоких арок ведут в разные крылья замка. Факелы освещают каменные стены с гербами. " +
                "В центре — дубовый стол с картами королевства.\n\n" +
                "Выбирай, куда направишься:\n" +
                "\uD83C\uDFF0 Замки — летописи твердынь\n" +
                "\uD83D\uDC65 Авторы — имена строителей и летописцев\n" +
                "\uD83C\uDFF7\uFE0F Титулы — звания и типы авторов\n" +
                "\uD83E\uDDF1 Материалы — камень, дерево, металл\n" +
                "\uD83D\uDD28 Реконструкции — история перестроек";

        SendMessage sm = new SendMessage(chatId.toString(), msg);
        sm.setReplyMarkup(KeyboardFactory.replyGrid(List.of(
                "\uD83C\uDFF0 Замки",
                "\uD83D\uDC65 Авторы",
                "\uD83C\uDFF7\uFE0F Титулы",
                "\uD83E\uDDF1 Материалы",
                "\uD83D\uDD28 Реконструкции",
                "\uD83D\uDEAA Выйти из замка"
        ), 2));
        return sm;
    }
}
