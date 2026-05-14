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
public class ReconstructionHandler implements CommandHandler {

    private final CastleKeeperApiClient api;
    private final DialogStateMachine sm;
    static final ReplyKeyboardMarkup SUBMENU = KeyboardFactory.replyGrid(List.of(
            "\uD83D\uDCDC Все реконструкции", "\uD83D\uDC41\uFE0F Выбрать реконстр.",
            "\uD83C\uDFD7\uFE0F Добавить реконстр.", "\u2712\uFE0F Изменить реконстр.", "\uD83D\uDCA5 Удалить реконстр.",
            "\u21A9\uFE0F Назад в меню"), 2);

    public ReconstructionHandler(CastleKeeperApiClient api, DialogStateMachine sm) { this.api = api; this.sm = sm; }

    @Override public SendMessage handle(Long chatId, String text) { return submenu(chatId); }

    public SendMessage list(Long chatId) {
        try {
            List<ReconstructionResponse> list = api.getReconstructions();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDD28 Архив реконструкций пуст.");
            return m(chatId, "\uD83D\uDD28 Реконструкции:\n\n" + list.stream().map(r -> "\u2022 " + (r.getCastle() != null ? r.getCastle().getName() : "?") + " — " + r.getReconstructionYear() + " (ID:" + r.getId() + ")").collect(Collectors.joining("\n")));
        } catch (Exception e) { return err(chatId); }
    }

    public SendMessage handleCallback(Long chatId, CallbackData cb) {
        return switch (cb.action()) {
            case Actions.MENU -> submenu(chatId);
            case Actions.LIST -> list(chatId);
            case Actions.VIEW -> pickView(chatId, cb);
            case Actions.ADD -> addWizard(chatId, cb);
            case Actions.EDIT -> pickEdit(chatId, cb);
            case Actions.DELETE -> pickDelete(chatId, cb);
            case Actions.CONFIRM_DELETE -> confirmDelete(chatId, cb);
            default -> submenu(chatId);
        };
    }

    private SendMessage submenu(Long chatId) { SendMessage s = m(chatId, "\uD83D\uDD28 Крыло Реконструкций.\n\nНа стенах — чертежи и схемы перестроек замков. Здесь записана история каждого обновления твердынь.\n\nЧто хочешь изучить?"); s.setReplyMarkup(SUBMENU); return s; }

    private SendMessage pickView(Long chatId, CallbackData cb) {
        try {
            List<ReconstructionResponse> list = api.getReconstructions();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDD28 Архив пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int pg = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, pg, Actions.VIEW); }
            ReconstructionResponse r = api.getReconstruction(cb.argAsLong(0));
            String info = "\uD83D\uDD28 Реконструкция (ID:" + r.getId() + ")\n\uD83D\uDCC5 Год: " + r.getReconstructionYear();
            if (r.getCastle() != null) info += "\n\uD83C\uDFF0 Замок: " + r.getCastle().getName();
            if (r.getAuthor() != null) info += "\n\uD83D\uDC64 Автор: " + r.getAuthor().getName();
            return m(chatId, info);
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage addWizard(Long chatId, CallbackData cb) {
        if (!cb.hasArgs()) {
            try {
                List<CastleResponse> castles = api.getCastles();
                if (castles.isEmpty()) return m(chatId, "\u26A0\uFE0F Сначала добавь замки.");
                sm.start(chatId, (cid, input) -> handleCallback(cid, CallbackData.parse(Actions.RECONSTRUCTION + ":add:castle:" + input)));
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(castles, 0, 5, c -> c.getName() + " (ID:" + c.getId() + ")", c -> Actions.RECONSTRUCTION + ":add:castle:" + c.getId(), Actions.RECONSTRUCTION + ":add:page_castle:0");
                SendMessage s = m(chatId, "\uD83C\uDFF0 Выбери замок:"); s.setReplyMarkup(kb); return s;
            } catch (Exception e) { return err(chatId); }
        }
        return switch (cb.arg(0)) {
            case "page_castle" -> { try { List<CastleResponse> cs = api.getCastles(); int pg = cb.args().size() > 1 ? parseInt(cb.arg(1), 0) : 0; InlineKeyboardMarkup kb = KeyboardFactory.paginated(cs, pg, 5, c -> c.getName() + " (ID:" + c.getId() + ")", c -> Actions.RECONSTRUCTION + ":add:castle:" + c.getId(), Actions.RECONSTRUCTION + ":add:page_castle:" + pg); SendMessage s = m(chatId, "\uD83C\uDFF0 Выбери замок:"); s.setReplyMarkup(kb); yield s; } catch (Exception e) { yield err(chatId); } }
            case "castle" -> addStep2(chatId, cb.argAsLong(1));
            case "author" -> addStep3(chatId, cb.argAsLong(1), cb.argAsLong(2));
            case "year" -> finishAdd(chatId, cb.argAsLong(1), cb.argAsLong(2), cb.argAsInt(3));
            default -> submenu(chatId);
        };
    }

    private SendMessage addStep2(Long chatId, Long castleId) {
        try {
            List<AuthorResponse> authors = api.getAuthors();
            if (authors.isEmpty()) return m(chatId, "\u26A0\uFE0F Сначала добавь авторов.");
            sm.start(chatId, (cid, input) -> handleCallback(cid, CallbackData.parse(Actions.RECONSTRUCTION + ":add:author:" + castleId + ":" + input)));
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(authors, 0, 5, a -> a.getName() + " (ID:" + a.getId() + ")", a -> Actions.RECONSTRUCTION + ":add:author:" + castleId + ":" + a.getId(), Actions.RECONSTRUCTION + ":add:page_author:" + castleId + ":0");
            SendMessage s = m(chatId, "\uD83D\uDC64 Выбери автора:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage addStep3(Long chatId, Long castleId, Long authorId) {
        sm.start(chatId, (cid, input) -> handleCallback(cid, CallbackData.parse(Actions.RECONSTRUCTION + ":add:year:" + castleId + ":" + authorId + ":" + input)));
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCC5 Введи год реконструкции:"));
    }

    private SendMessage finishAdd(Long chatId, Long castleId, Long authorId, int year) {
        try { api.createReconstruction(new ReconstructionRequest(castleId, authorId, year)); sm.cancel(chatId); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickEdit(Long chatId, CallbackData cb) {
        try {
            List<ReconstructionResponse> list = api.getReconstructions();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDD28 Архив пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int pg = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, pg, Actions.EDIT); }
            Long id = cb.argAsLong(0);
            sm.start(chatId, (cid, input) -> {
                try { ReconstructionResponse r = api.getReconstruction(id); api.updateReconstruction(id, new ReconstructionRequest(r.getCastle() != null ? r.getCastle().getId() : 0L, r.getAuthor() != null ? r.getAuthor().getId() : 0L, Integer.parseInt(input))); sm.cancel(cid); return submenu(cid); }
                catch (Exception e) { return m(cid, errMsg(e)); }
            });
            return DialogStateMachine.removeKeyboard(m(chatId, "\u2712\uFE0F Введи новый год:"));
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage pickDelete(Long chatId, CallbackData cb) {
        try {
            List<ReconstructionResponse> list = api.getReconstructions();
            if (list.isEmpty()) return m(chatId, "\uD83D\uDD28 Архив пуст.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) { int pg = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0; return pickPage(chatId, list, pg, Actions.DELETE); }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(KeyboardFactory.btn("\u2705 Да", Actions.RECONSTRUCTION + ":confirm_delete:" + id), KeyboardFactory.btn("\u274C Нет", Actions.CANCEL)), 2);
            SendMessage s = m(chatId, "\u26A0\uFE0F Удалить реконструкцию ID:" + id + "?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmDelete(Long chatId, CallbackData cb) {
        try { api.deleteReconstruction(cb.argAsLong(0)); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    private SendMessage pickPage(Long chatId, List<ReconstructionResponse> list, int page, String action) {
        InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, r -> (r.getCastle() != null ? r.getCastle().getName() : "?") + " " + r.getReconstructionYear(), r -> Actions.RECONSTRUCTION + ":" + action + ":" + r.getId(), Actions.RECONSTRUCTION + ":" + action + ":page");
        SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери:"); s.setReplyMarkup(kb); return s;
    }

    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    private String errMsg(Exception e) { String msg = e.getMessage(); return "\u26A0\uFE0F " + (msg != null && msg.length() > 200 ? msg.substring(0, 200) : msg); }
    private SendMessage m(Long cid, String t) { return new SendMessage(cid.toString(), t); }
    private SendMessage err(Long cid) { return m(cid, "\u26A0\uFE0F Сервер замка не отвечает."); }
}
