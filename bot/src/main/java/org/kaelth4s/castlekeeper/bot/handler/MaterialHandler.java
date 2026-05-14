package org.kaelth4s.castlekeeper.bot.handler;

import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.keyboard.KeyboardFactory;
import org.kaelth4s.castlekeeper.dto.MaterialRequest;
import org.kaelth4s.castlekeeper.dto.MaterialResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MaterialHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(MaterialHandler.class);
    private final CastleKeeperApiClient api;
    private final DialogStateMachine sm;

    static final ReplyKeyboardMarkup SUBMENU = KeyboardFactory.replyGrid(List.of(
            "\uD83D\uDCDC Все материалы", "\uD83D\uDC41\uFE0F Выбрать материал",
            "\uD83C\uDFD7\uFE0F Добавить материал", "\u2712\uFE0F Изменить материал", "\uD83D\uDCA5 Удалить материал",
            "\u21A9\uFE0F Назад в меню"), 2);

    public MaterialHandler(CastleKeeperApiClient api, DialogStateMachine sm) { this.api = api; this.sm = sm; }

    @Override public SendMessage handle(Long chatId, String text) { return submenu(chatId); }

    public SendMessage list(Long chatId) {
        try {
            List<MaterialResponse> mats = api.getMaterials();
            if (mats.isEmpty()) return m(chatId, "\uD83E\uDDF1 Каменоломня пуста.");
            return m(chatId, "\uD83E\uDDF1 Материалы:\n\n" + mats.stream().map(x -> "\u2022 " + x.getName() + " (ID:" + x.getId() + ")").collect(Collectors.joining("\n")));
        } catch (Exception e) { return err(chatId); }
    }

    public SendMessage handleCallback(Long chatId, CallbackData cb) {
        return switch (cb.action()) {
            case Actions.MENU -> submenu(chatId);
            case Actions.LIST -> list(chatId);
            case Actions.VIEW -> pickView(chatId, cb);
            case Actions.ADD -> addWizard(chatId);
            case Actions.EDIT -> pickEdit(chatId, cb);
            case Actions.DELETE -> pickDelete(chatId, cb);
            case Actions.CONFIRM_DELETE -> confirmDelete(chatId, cb);
            default -> submenu(chatId);
        };
    }

    private SendMessage submenu(Long chatId) { SendMessage s = m(chatId, "\uD83E\uDDF1 Крыло Материалов.\n\nВдоль стен — ящики с образцами: гранит, известняк, дуб, железо.\n\nЧто изучить или добавить?"); s.setReplyMarkup(SUBMENU); return s; }

    private SendMessage pickView(Long chatId, CallbackData cb) {
        try {
            List<MaterialResponse> mats = api.getMaterials();
            if (mats.isEmpty()) return m(chatId, "\uD83E\uDDF1 Каменоломня пуста.");
            int page = cb.hasArgs() ? parseInt(cb.arg(0), 0) : 0;
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) {
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, page, 5,
                        x -> x.getName() + " (ID:" + x.getId() + ")", x -> Actions.MATERIAL + ":view:" + x.getId(), Actions.MATERIAL + ":view:page");
                SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери материал:"); s.setReplyMarkup(kb); return s;
            }
            Long id = cb.argAsLong(0);
            MaterialResponse r = api.getMaterial(id);
            return m(chatId, "\uD83E\uDDF1 " + r.getName() + " (ID:" + r.getId() + ")");
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage addWizard(Long chatId) {
        sm.start(chatId, (cid, input) -> {
            try { api.createMaterial(new MaterialRequest(input.trim())); return submenu(cid); }
            catch (Exception e) { return m(cid, errMsg(e)); }
        });
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83C\uDFD7\uFE0F Введи название нового материала:"));
    }

    private SendMessage pickEdit(Long chatId, CallbackData cb) {
        try {
            List<MaterialResponse> mats = api.getMaterials();
            if (mats.isEmpty()) return m(chatId, "\uD83E\uDDF1 Каменоломня пуста.");
            if (!cb.hasArgs()) {
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, 0, 5,
                        x -> x.getName() + " (ID:" + x.getId() + ")", x -> Actions.MATERIAL + ":edit:" + x.getId(), Actions.MATERIAL + ":edit:page");
                SendMessage s = m(chatId, "\u2712\uFE0F Выбери материал:"); s.setReplyMarkup(kb); return s;
            }
            if ("page".equals(cb.arg(0))) { int pg = parseInt(cb.arg(1), 0); return pickPage(chatId, mats, pg, Actions.EDIT, Actions.MATERIAL + ":edit:page"); }
            Long id = cb.argAsLong(0);
            sm.start(chatId, (cid, input) -> {
                try { api.updateMaterial(id, new MaterialRequest(input.trim())); return submenu(cid); }
                catch (Exception e) { return m(cid, errMsg(e)); }
            });
            return DialogStateMachine.removeKeyboard(m(chatId, "\u2712\uFE0F Введи новое название:"));
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage pickDelete(Long chatId, CallbackData cb) {
        try {
            List<MaterialResponse> mats = api.getMaterials();
            if (mats.isEmpty()) return m(chatId, "\uD83E\uDDF1 Каменоломня пуста.");
            if (!cb.hasArgs()) {
                return pickPage(chatId, mats, 0, Actions.DELETE, Actions.MATERIAL + ":delete:page");
            }
            if ("page".equals(cb.arg(0))) { int pg = parseInt(cb.arg(1), 0); return pickPage(chatId, mats, pg, Actions.DELETE, Actions.MATERIAL + ":delete:page"); }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(
                    KeyboardFactory.btn("\u2705 Да", Actions.MATERIAL + ":confirm_delete:" + id),
                    KeyboardFactory.btn("\u274C Нет", Actions.CANCEL)), 2);
            SendMessage s = m(chatId, "\u26A0\uFE0F Удалить материал ID:" + id + "?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmDelete(Long chatId, CallbackData cb) {
        try { api.deleteMaterial(cb.argAsLong(0)); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickPage(Long chatId, List<MaterialResponse> mats, int page, String action, String navPrefix) {
        InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, page, 5,
                x -> x.getName() + " (ID:" + x.getId() + ")", x -> Actions.MATERIAL + ":" + action + ":" + x.getId(), navPrefix);
        SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери материал:"); s.setReplyMarkup(kb); return s;
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    private String errMsg(Exception e) { String msg = e.getMessage(); return "\u26A0\uFE0F " + (msg != null && msg.length() > 200 ? msg.substring(0, 200) : msg); }
    private SendMessage m(Long cid, String t) { return new SendMessage(cid.toString(), t); }
    private SendMessage err(Long cid) { return m(cid, "\u26A0\uFE0F Сервер замка не отвечает."); }
}
