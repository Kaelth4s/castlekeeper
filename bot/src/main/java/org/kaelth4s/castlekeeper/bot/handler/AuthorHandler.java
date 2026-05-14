package org.kaelth4s.castlekeeper.bot.handler;

import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.keyboard.KeyboardFactory;
import org.kaelth4s.castlekeeper.dto.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorHandler implements CommandHandler {

    private final CastleKeeperApiClient api;
    private final DialogStateMachine sm;
    static final ReplyKeyboardMarkup SUBMENU = KeyboardFactory.replyGrid(List.of(
            "\uD83D\uDCDC Все авторы", "\uD83D\uDC41\uFE0F Выбрать автора",
            "\uD83C\uDFD7\uFE0F Добавить автора", "\u2712\uFE0F Изменить автора", "\uD83D\uDCA5 Удалить автора",
            "\u21A9\uFE0F Назад в меню"), 2);

    public AuthorHandler(CastleKeeperApiClient api, DialogStateMachine sm) { this.api = api; this.sm = sm; }

    @Override public SendMessage handle(Long chatId, String text) { return submenu(chatId); }

    public SendMessage list(Long chatId) {
        try {
            List<AuthorResponse> list = api.getAuthors();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDC65 Скрипторий пуст.");
            return m(chatId, "\uD83D\uDC65 Авторы:\n\n" + list.stream().map(a -> "\u2022 " + a.getName() + " (ID:" + a.getId() + ")" + (a.getAuthorType() != null ? " [" + a.getAuthorType().getName() + "]" : "")).collect(Collectors.joining("\n")));
        } catch (Exception e) { return err(chatId); }
    }

    public SendMessage handleCallback(Long chatId, CallbackData cb) {
        return switch (cb.action()) {
            case Actions.MENU -> submenu(chatId);
            case Actions.LIST -> list(chatId);
            case Actions.VIEW -> pickView(chatId, cb);
            case Actions.ADD -> addWizard(chatId, cb);
            case Actions.EDIT -> pickEdit(chatId, cb);
            case "edit_name" -> editName(chatId, cb);
            case "edit_type" -> editType(chatId, cb);
            case "edit_type_page" -> editTypePage(chatId, cb);
            case Actions.DELETE -> pickDelete(chatId, cb);
            case Actions.CONFIRM_DELETE -> confirmDelete(chatId, cb);
            default -> submenu(chatId);
        };
    }

    private SendMessage submenu(Long chatId) { SendMessage s = m(chatId, "\uD83D\uDC65 Крыло Авторов — скрипторий.\n\nВдоль стен — дубовые шкафы с пергаментами. Здесь записаны имена строителей и летописцев.\n\nКого хочешь найти или вписать в анналы?"); s.setReplyMarkup(SUBMENU); return s; }

    private SendMessage pickView(Long chatId, CallbackData cb) {
        try {
            List<AuthorResponse> list = api.getAuthors();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDC65 Скрипторий пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, page, Actions.VIEW); }
            AuthorResponse a = api.getAuthor(cb.argAsLong(0));
            String info = "\uD83D\uDC64 " + a.getName() + " (ID:" + a.getId() + ")";
            if (a.getAuthorType() != null) info += "\n\uD83C\uDFF7\uFE0F " + a.getAuthorType().getName() + (a.getAuthorType().getDescription() != null ? " — " + a.getAuthorType().getDescription() : "");
            return m(chatId, info);
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage addWizard(Long chatId, CallbackData cb) {
        if (!cb.hasArgs()) {
            sm.start(chatId, (cid, input) -> pickTypeForAdd(cid, input));
            return DialogStateMachine.removeKeyboard(m(chatId, "\uD83C\uDFD7\uFE0F Введи имя автора:"));
        }
        return switch (cb.arg(0)) {
            case "type" -> confirmAdd(chatId, cb.arg(1), cb.argAsLong(2));
            case "page_type" -> typePage(chatId, parseInt(cb.arg(cb.args().size()-1), 0));
            default -> submenu(chatId);
        };
    }

    private SendMessage pickTypeForAdd(Long chatId, String name) {
        try {
            List<AuthorTypeResponse> types = api.getAuthorTypes();
            if (types.isEmpty()) return m(chatId, "\u26A0\uFE0F Сначала добавь типы.");
            sm.cancel(chatId);
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(types, 0, 5,
                    t -> t.getName() + " (ID:" + t.getId() + ")",
                    t -> Actions.AUTHOR + ":add:type:" + name + ":" + t.getId(),
                    Actions.AUTHOR + ":add:page_type:" + name + ":");
            SendMessage s = m(chatId, "\uD83C\uDFF7\uFE0F Выбери тип автора:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage typePage(Long chatId, int pg) {
        try {
            // Extract name from the paginated callback context isn't stored — reconstruct from args
            // For simplicity, just rebuild keyboard with default
            List<AuthorTypeResponse> types = api.getAuthorTypes();
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(types, pg, 5,
                    t -> t.getName() + " (ID:" + t.getId() + ")",
                    t -> Actions.AUTHOR + ":add:type:" + t.getId(),
                    Actions.AUTHOR + ":add:page_type:" + pg);
            SendMessage s = m(chatId, "\uD83C\uDFF7\uFE0F Выбери тип:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmAdd(Long chatId, String name, Long typeId) {
        try { api.createAuthor(new AuthorRequest(name, typeId)); sm.cancel(chatId); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickEdit(Long chatId, CallbackData cb) {
        try {
            List<AuthorResponse> list = api.getAuthors();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDC65 Скрипторий пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, page, Actions.EDIT); }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(KeyboardFactory.btn("\uD83D\uDC64 Имя", Actions.AUTHOR + ":edit_name:" + id), KeyboardFactory.btn("\uD83C\uDFF7\uFE0F Тип", Actions.AUTHOR + ":edit_type:" + id)), 2);
            SendMessage s = m(chatId, "\u2712\uFE0F Автор ID:" + id + " — что меняем?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage editName(Long chatId, CallbackData cb) {
        Long id = cb.argAsLong(0);
        sm.start(chatId, (cid, input) -> {
            try { AuthorResponse a = api.getAuthor(id); api.updateAuthor(id, new AuthorRequest(input, a.getAuthorType() != null ? a.getAuthorType().getId() : 0L)); sm.cancel(cid); return submenu(cid); }
            catch (Exception e) { return m(cid, errMsg(e)); }
        });
        return DialogStateMachine.removeKeyboard(m(chatId, "\u2712\uFE0F Введи новое имя:"));
    }

    private SendMessage editType(Long chatId, CallbackData cb) {
        Long id = cb.argAsLong(0);
        if (cb.args().size() >= 2) { Long tid = cb.argAsLong(1); return applyEditType(chatId, id, tid); }
        try {
            List<AuthorTypeResponse> types = api.getAuthorTypes();
            if (types.isEmpty()) return m(chatId, "\u26A0\uFE0F Нет типов.");
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(types, 0, 5, t -> t.getName() + " (ID:" + t.getId() + ")", t -> Actions.AUTHOR + ":edit_type:" + id + ":" + t.getId(), Actions.AUTHOR + ":edit_type_page:" + id);
            SendMessage s = m(chatId, "\uD83C\uDFF7\uFE0F Выбери новый тип:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage editTypePage(Long chatId, CallbackData cb) {
        Long id = cb.argAsLong(0); int pg = cb.args().size() >= 2 ? parseInt(cb.arg(1), 0) : 0;
        try {
            List<AuthorTypeResponse> types = api.getAuthorTypes();
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(types, pg, 5, t -> t.getName() + " (ID:" + t.getId() + ")", t -> Actions.AUTHOR + ":edit_type:" + id + ":" + t.getId(), Actions.AUTHOR + ":edit_type_page:" + id);
            SendMessage s = m(chatId, "\uD83C\uDFF7\uFE0F Выбери тип:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage applyEditType(Long chatId, Long id, Long tid) {
        try { AuthorResponse a = api.getAuthor(id); api.updateAuthor(id, new AuthorRequest(a.getName(), tid)); sm.cancel(chatId); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickDelete(Long chatId, CallbackData cb) {
        try {
            List<AuthorResponse> list = api.getAuthors();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDC65 Скрипторий пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, page, Actions.DELETE); }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(KeyboardFactory.btn("\u2705 Да", Actions.AUTHOR + ":confirm_delete:" + id), KeyboardFactory.btn("\u274C Нет", Actions.CANCEL)), 2);
            SendMessage s = m(chatId, "\u26A0\uFE0F Удалить автора ID:" + id + "?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmDelete(Long chatId, CallbackData cb) {
        try { api.deleteAuthor(cb.argAsLong(0)); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickPage(Long chatId, List<AuthorResponse> list, int page, String action) {
        InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, a -> a.getName() + " (ID:" + a.getId() + ")", a -> Actions.AUTHOR + ":" + action + ":" + a.getId(), Actions.AUTHOR + ":" + action + ":page");
        SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери автора:"); s.setReplyMarkup(kb); return s;
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    private String errMsg(Exception e) { String msg = e.getMessage(); return "\u26A0\uFE0F " + (msg != null && msg.length() > 200 ? msg.substring(0, 200) : msg); }
    private SendMessage m(Long cid, String t) { return new SendMessage(cid.toString(), t); }
    private SendMessage err(Long cid) { return m(cid, "\u26A0\uFE0F Сервер замка не отвечает."); }
}
