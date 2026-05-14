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
public class AuthorTypeHandler implements CommandHandler {

    private final CastleKeeperApiClient api;
    private final DialogStateMachine sm;
    static final ReplyKeyboardMarkup SUBMENU = KeyboardFactory.replyGrid(List.of(
            "\uD83D\uDCDC Все титулы", "\uD83C\uDFD7\uFE0F Добавить титул",
            "\u2712\uFE0F Изменить титул", "\uD83D\uDCA5 Удалить титул",
            "\u21A9\uFE0F Назад в меню"), 2);

    public AuthorTypeHandler(CastleKeeperApiClient api, DialogStateMachine sm) { this.api = api; this.sm = sm; }

    @Override public SendMessage handle(Long chatId, String text) { return submenu(chatId); }

    public SendMessage list(Long chatId) {
        try {
            List<AuthorTypeResponse> list = api.getAuthorTypes();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF7\uFE0F Зал титулов пуст.");
            return m(chatId, "\uD83C\uDFF7\uFE0F Титулы:\n\n" + list.stream().map(t -> "\u2022 " + t.getName() + " (ID:" + t.getId() + ")" + (t.getDescription() != null ? " — " + t.getDescription() : "")).collect(Collectors.joining("\n")));
        } catch (Exception e) { return err(chatId); }
    }

    public SendMessage handleCallback(Long chatId, CallbackData cb) {
        return switch (cb.action()) {
            case Actions.MENU -> submenu(chatId);
            case Actions.LIST -> list(chatId);
            case Actions.ADD -> addWizard(chatId, cb);
            case Actions.EDIT -> pickEdit(chatId, cb);
            case Actions.DELETE -> pickDelete(chatId, cb);
            case Actions.CONFIRM_DELETE -> confirmDelete(chatId, cb);
            default -> submenu(chatId);
        };
    }

    private SendMessage submenu(Long chatId) { SendMessage s = m(chatId, "\uD83C\uDFF7\uFE0F Зал Титулов.\n\nНа стенах висят гербы с именами званий: Строитель, Летописец, Архитектор, Хронист.\n\nЧто хочешь сделать?"); s.setReplyMarkup(SUBMENU); return s; }

    private SendMessage addWizard(Long chatId, CallbackData cb) {
        if (!cb.hasArgs()) { sm.start(chatId, (cid, input) -> addStep2(cid, input)); return DialogStateMachine.removeKeyboard(m(chatId, "\uD83C\uDFD7\uFE0F Введи название титула:")); }
        return switch (cb.arg(0)) { case "desc" -> finishAdd(chatId, cb.arg(1), cb.arg(2)); default -> submenu(chatId); };
    }

    private SendMessage addStep2(Long chatId, String name) {
        sm.start(chatId, (cid, input) -> handleCallback(cid, CallbackData.parse(Actions.AUTHOR_TYPE + ":add:desc:" + name + ":" + input)));
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCDD Введи описание (или -):"));
    }

    private SendMessage finishAdd(Long chatId, String name, String desc) {
        try { api.createAuthorType(new AuthorTypeRequest(name, "-".equals(desc) ? "" : desc)); sm.cancel(chatId); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickEdit(Long chatId, CallbackData cb) {
        try {
            List<AuthorTypeResponse> list = api.getAuthorTypes();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF7\uFE0F Зал титулов пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int pg = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, pg, Actions.EDIT); }
            Long id = cb.argAsLong(0);
            sm.start(chatId, (cid, input) -> { try { api.updateAuthorType(id, new AuthorTypeRequest(input, null)); sm.cancel(cid); return submenu(cid); } catch (Exception e) { return m(cid, errMsg(e)); } });
            return DialogStateMachine.removeKeyboard(m(chatId, "\u2712\uFE0F Введи новое название:"));
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage pickDelete(Long chatId, CallbackData cb) {
        try {
            List<AuthorTypeResponse> list = api.getAuthorTypes();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF7\uFE0F Зал титулов пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int pg = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, pg, Actions.DELETE); }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(KeyboardFactory.btn("\u2705 Да", Actions.AUTHOR_TYPE + ":confirm_delete:" + id), KeyboardFactory.btn("\u274C Нет", Actions.CANCEL)), 2);
            SendMessage s = m(chatId, "\u26A0\uFE0F Удалить титул ID:" + id + "?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmDelete(Long chatId, CallbackData cb) {
        try { api.deleteAuthorType(cb.argAsLong(0)); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickPage(Long chatId, List<AuthorTypeResponse> list, int page, String action) {
        InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, t -> t.getName() + " (ID:" + t.getId() + ")", t -> Actions.AUTHOR_TYPE + ":" + action + ":" + t.getId(), Actions.AUTHOR_TYPE + ":" + action + ":page");
        SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери титул:"); s.setReplyMarkup(kb); return s;
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    private String errMsg(Exception e) { String msg = e.getMessage(); return "\u26A0\uFE0F " + (msg != null && msg.length() > 200 ? msg.substring(0, 200) : msg); }
    private SendMessage m(Long cid, String t) { return new SendMessage(cid.toString(), t); }
    private SendMessage err(Long cid) { return m(cid, "\u26A0\uFE0F Сервер замка не отвечает."); }
}
