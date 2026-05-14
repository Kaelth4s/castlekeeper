package org.kaelth4s.castlekeeper.bot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KeyboardFactory {

    public static final String BACK = "/menu";

    // ---- ReplyKeyboard (navigation, appears below text input) ----

    public static ReplyKeyboardMarkup replyGrid(List<String> labels, int cols) {
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (int i = 0; i < labels.size(); i += cols) {
            KeyboardRow row = new KeyboardRow();
            int end = Math.min(i + cols, labels.size());
            for (int j = i; j < end; j++) {
                row.add(labels.get(j));
            }
            keyboard.add(row);
        }
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboard);
        markup.setResizeKeyboard(true);
        return markup;
    }

    // ---- InlineKeyboard (CRUD pickers, appears inside chat) ----

    public static InlineKeyboardMarkup inlineGrid(List<InlineKeyboardButton> buttons, int cols) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += cols) {
            int end = Math.min(i + cols, buttons.size());
            keyboard.add(new ArrayList<>(buttons.subList(i, end)));
        }
        return new InlineKeyboardMarkup(keyboard);
    }

    /**
     * Builds a paginated inline list: 5 items per page + ◀ ▶ navigation.
     * callbackPrefix should include :page: suffix for the page number to be appended.
     * Example: "material:view:page:" → button callback: "material:view:page:0"
     */
    public static <T> InlineKeyboardMarkup paginated(
            List<T> items, int page, int pageSize,
            Function<T, String> labelFn, Function<T, String> callbackFn,
            String navPrefix) {

        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, items.size());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (int i = start; i < end; i++) {
            T item = items.get(i);
            keyboard.add(List.of(btn(labelFn.apply(item), callbackFn.apply(item))));
        }

        if (totalPages > 1) {
            List<InlineKeyboardButton> navRow = new ArrayList<>();
            navRow.add(btn(page + 1 + "/" + totalPages, "ignore"));
            if (page > 0) {
                navRow.add(btn("◀", navPrefix + ":" + (page - 1)));
            }
            if (page < totalPages - 1) {
                navRow.add(btn("▶", navPrefix + ":" + (page + 1)));
            }
            keyboard.add(navRow);
        }

        keyboard.add(List.of(btn("↩️ Отмена", "cancel")));
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardButton btn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
