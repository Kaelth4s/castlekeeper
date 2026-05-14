package org.kaelth4s.castlekeeper.bot.handler;

import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStep;
import org.kaelth4s.castlekeeper.bot.keyboard.KeyboardFactory;
import org.kaelth4s.castlekeeper.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CastleHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CastleHandler.class);
    private final CastleKeeperApiClient api;
    private final DialogStateMachine sm;

    static final ReplyKeyboardMarkup SUBMENU = KeyboardFactory.replyGrid(List.of(
            "\uD83D\uDCDC Все замки", "\uD83D\uDC41\uFE0F Выбрать замок", "\uD83C\uDFB2 Случайный замок",
            "\uD83C\uDFD7\uFE0F Добавить замок", "\u2712\uFE0F Изменить замок", "\uD83D\uDCA5 Удалить замок",
            "\u21A9\uFE0F Назад в меню"), 3);

    public CastleHandler(CastleKeeperApiClient api, DialogStateMachine sm) { this.api = api; this.sm = sm; }

    @Override public SendMessage handle(Long chatId, String text) { return submenu(chatId); }

    public SendMessage list(Long chatId) {
        try {
            List<CastleResponse> list = api.getCastles();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF0 Летописи пусты.");
            return m(chatId, "\uD83C\uDFF0 Замки:\n\n" + list.stream().map(c -> "\u2022 " + c.getName() + " (ID:" + c.getId() + ")").collect(Collectors.joining("\n")));
        } catch (Exception e) { return err(chatId); }
    }

    public SendMessage handleCallback(Long chatId, CallbackData cb) {
        return switch (cb.action()) {
            case Actions.MENU -> submenu(chatId);
            case Actions.LIST -> list(chatId);
            case Actions.VIEW -> pickView(chatId, cb);
            case Actions.RANDOM -> random(chatId);
            case Actions.ADD -> addWizard(chatId, cb);
            case Actions.EDIT -> pickEdit(chatId, cb);
            case "edit_name" -> editTextField(chatId, cb, "name");
            case "edit_desc" -> editTextField(chatId, cb, "desc");
            case "edit_built" -> editTextField(chatId, cb, "built");
            case "edit_destroyed" -> editTextField(chatId, cb, "destroyed");
            case "edit_height" -> editTextField(chatId, cb, "height");
            case "edit_author" -> editFkField(chatId, cb, "author");
            case "edit_material" -> editFkField(chatId, cb, "material");
            case "edit_page_author" -> editFkPage(chatId, cb, "author");
            case "edit_page_material" -> editFkPage(chatId, cb, "material");
            case Actions.DELETE -> pickDelete(chatId, cb);
            case Actions.CONFIRM_DELETE -> confirmDelete(chatId, cb);
            default -> submenu(chatId);
        };
    }

    private SendMessage submenu(Long chatId) { SendMessage s = m(chatId, "\uD83C\uDFF0 Крыло Замков.\n\nВдоль стен — дубовые стеллажи со свитками. Здесь хранятся летописи всех твердынь мира.\n\nЧто желаешь сделать, хранитель?"); s.setReplyMarkup(SUBMENU); return s; }

    private SendMessage random(Long chatId) {
        try { CastleResponse c = api.getRandomCastle(); return c != null ? m(chatId, fmt(c)) : m(chatId, "\uD83C\uDFF0 Летописи пусты."); }
        catch (Exception e) { return err(chatId); }
    }

    private SendMessage pickView(Long chatId, CallbackData cb) {
        try {
            List<CastleResponse> list = api.getCastles();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF0 Летописи пусты.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) {
                int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0;
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, c -> c.getName() + " (ID:" + c.getId() + ")", c -> Actions.CASTLE + ":view:" + c.getId(), Actions.CASTLE + ":view:page");
                SendMessage s = m(chatId, "\uD83D\uDC41\uFE0F Выбери замок:"); s.setReplyMarkup(kb); return s;
            }
            return m(chatId, fmt(api.getCastle(cb.argAsLong(0))));
        } catch (Exception e) { return err(chatId); }
    }

    // ---- Add Wizard ----
    private SendMessage addWizard(Long chatId, CallbackData cb) {
        if (!cb.hasArgs()) {
            sm.start(chatId, (cid, input) -> addWizardStep2(cid, input));
            return DialogStateMachine.removeKeyboard(m(chatId, "\uD83C\uDFD7\uFE0F Шаг 1/7. Введи название замка:"));
        }
        return switch (cb.arg(0)) {
            case "name" -> addWizardStep2(chatId, cb.arg(1));
            case "desc" -> addWizardStep3(chatId, cb.arg(1), cb.arg(2));
            case "author" -> addWizardStep4(chatId, cb.arg(1), cb.arg(2), cb.argAsLong(3));
            case "built" -> addWizardStep5(chatId, cb.arg(1), cb.arg(2), cb.argAsLong(3), cb.arg(4));
            case "destroyed" -> addWizardStep6(chatId, cb.arg(1), cb.arg(2), cb.argAsLong(3), cb.arg(4), cb.arg(5));
            case "height" -> addWizardStep7(chatId, cb.arg(1), cb.arg(2), cb.argAsLong(3), cb.arg(4), cb.arg(5), cb.arg(6));
            case "material" -> createCastle(chatId, cb.arg(1), cb.arg(2), cb.argAsLong(3), cb.arg(4), cb.arg(5), cb.arg(6), cb.argAsLong(7));
            case "page_author" -> addWizardAuthorPage(chatId, cb.args());
            case "page_material" -> addWizardMaterialPage(chatId, cb.args());
            default -> submenu(chatId);
        };
    }

    private SendMessage addWizardStep2(Long chatId, String name) {
        sm.start(chatId, (cid, input) -> handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:desc:" + name + ":" + input)));
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCDD Шаг 2/7. Введи описание:"));
    }
    private SendMessage addWizardStep3(Long chatId, String name, String desc) {
        try {
            List<AuthorResponse> authors = api.getAuthors();
            if (authors.isEmpty()) return m(chatId, "\u26A0\uFE0F Сначала добавь авторов.");
            // NO state machine — callback buttons handle author selection
            sm.cancel(chatId);
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(authors, 0, 5,
                    a -> a.getName() + " (ID:" + a.getId() + ")",
                    a -> Actions.CASTLE + ":add:author:" + name + ":" + desc + ":" + a.getId(),
                    Actions.CASTLE + ":add:page_author:" + name + ":" + desc);
            SendMessage s = m(chatId, "\uD83D\uDC64 Шаг 3/7. Выбери автора:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }
    private SendMessage addWizardAuthorPage(Long chatId, List<String> args) {
        // args = [page_author, name, desc, pageNum]
        String name = args.size() > 1 ? args.get(1) : "";
        String desc = args.size() > 2 ? args.get(2) : "";
        int page = args.size() > 3 ? parseInt(args.get(args.size()-1), 0) : 0;
        try {
            List<AuthorResponse> authors = api.getAuthors();
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(authors, page, 5,
                    a -> a.getName() + " (ID:" + a.getId() + ")",
                    a -> Actions.CASTLE + ":add:author:" + name + ":" + desc + ":" + a.getId(),
                    Actions.CASTLE + ":add:page_author:" + name + ":" + desc);
            SendMessage s = m(chatId, "\uD83D\uDC64 Выбери автора:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }
    private SendMessage addWizardStep4(Long chatId, String name, String desc, Long authorId) {
        sm.start(chatId, (cid, input) -> {
            if (invalid(input, true)) { sm.start(chatId, stepForStep4(name, desc, authorId)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); }
            return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:built:" + name + ":" + desc + ":" + authorId + ":" + input));
        });
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCC5 Шаг 4/7. Введи год основания (или -):"));
    }
    private DialogStep stepForStep4(String name, String desc, Long authorId) {
        return (cid, input) -> {
            if (invalid(input, true)) { sm.start(cid, stepForStep4(name, desc, authorId)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); }
            return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:built:" + name + ":" + desc + ":" + authorId + ":" + input));
        };
    }
    private SendMessage addWizardStep5(Long chatId, String name, String desc, Long authorId, String built) {
        sm.start(chatId, (cid, input) -> {
            if (invalid(input, true)) { sm.start(cid, retryStep5(name, desc, authorId, built)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); }
            return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:destroyed:" + name + ":" + desc + ":" + authorId + ":" + built + ":" + input));
        });
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCA5 Шаг 5/7. Введи год разрушения (или -):"));
    }
    private DialogStep retryStep5(String name, String desc, Long authorId, String built) { return (cid, input) -> { if (invalid(input, true)) { sm.start(cid, retryStep5(name, desc, authorId, built)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); } return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:destroyed:" + name + ":" + desc + ":" + authorId + ":" + built + ":" + input)); }; }
    private SendMessage addWizardStep6(Long chatId, String name, String desc, Long authorId, String built, String destroyed) {
        sm.start(chatId, (cid, input) -> {
            if (invalid(input, true)) { sm.start(cid, retryStep6(name, desc, authorId, built, destroyed)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); }
            return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:height:" + name + ":" + desc + ":" + authorId + ":" + built + ":" + destroyed + ":" + input));
        });
        return DialogStateMachine.removeKeyboard(m(chatId, "\uD83D\uDCCF Шаг 6/7. Введи высоту (м) (или -):"));
    }
    private DialogStep retryStep6(String name, String desc, Long authorId, String built, String destroyed) { return (cid, input) -> { if (invalid(input, true)) { sm.start(cid, retryStep6(name, desc, authorId, built, destroyed)); return DialogStateMachine.removeKeyboard(m(cid, "\u26A0\uFE0F Введи число (или -):")); } return handleCallback(cid, CallbackData.parse(Actions.CASTLE + ":add:height:" + name + ":" + desc + ":" + authorId + ":" + built + ":" + destroyed + ":" + input)); }; }
    private SendMessage addWizardStep7(Long chatId, String name, String desc, Long authorId, String built, String destroyed, String height) {
        try {
            List<MaterialResponse> mats = api.getMaterials();
            if (mats.isEmpty()) return m(chatId, "\u26A0\uFE0F Сначала добавь материалы.");
            sm.cancel(chatId);
            String ctx = name + ":" + desc + ":" + authorId + ":" + built + ":" + destroyed + ":" + height;
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, 0, 5,
                    x -> x.getName() + " (ID:" + x.getId() + ")",
                    x -> Actions.CASTLE + ":add:material:" + ctx + ":" + x.getId(),
                    Actions.CASTLE + ":add:page_material:" + ctx);
            SendMessage s = m(chatId, "\uD83E\uDDF1 Шаг 7/7. Выбери материал:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }
    private SendMessage addWizardMaterialPage(Long chatId, List<String> args) {
        try {
            // args: [page_material, name, desc, authorId, built, destroyed, height, pageNum]
            int page = args.size() > 7 ? parseInt(args.get(args.size()-1), 0) : 0;
            List<MaterialResponse> mats = api.getMaterials();
            String ctx = args.get(1) + ":" + args.get(2) + ":" + args.get(3) + ":" + args.get(4) + ":" + args.get(5) + ":" + args.get(6);
            InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, page, 5,
                    x -> x.getName() + " (ID:" + x.getId() + ")",
                    x -> Actions.CASTLE + ":add:material:" + ctx + ":" + x.getId(),
                    Actions.CASTLE + ":add:page_material:" + ctx);
            SendMessage s = m(chatId, "\uD83E\uDDF1 Выбери материал:"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage createCastle(Long chatId, String name, String desc, Long authorId, String built, String destroyed, String height, Long materialId) {
        try {
            CastleRequest req = new CastleRequest(); req.setName(name); req.setDescription(desc); req.setAuthorId(authorId);
            req.setBuiltYear("-".equals(built) ? null : Integer.parseInt(built));
            req.setDestroyedYear("-".equals(destroyed) ? null : Integer.parseInt(destroyed));
            req.setHeightM("-".equals(height) ? null : new BigDecimal(height));
            req.setMaterialId(materialId);
            api.createCastle(req);
            sm.cancel(chatId);
            return submenu(chatId);
        } catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    // ---- Edit ----
    private SendMessage pickEdit(Long chatId, CallbackData cb) {
        try {
            List<CastleResponse> list = api.getCastles();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF0 Летописи пусты.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) {
                int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0;
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, c -> c.getName() + " (ID:" + c.getId() + ")", c -> Actions.CASTLE + ":edit:" + c.getId(), Actions.CASTLE + ":edit:page");
                SendMessage s = m(chatId, "\u2712\uFE0F Выбери замок:"); s.setReplyMarkup(kb); return s;
            }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(
                    KeyboardFactory.btn("\uD83C\uDFF7\uFE0F Название", Actions.CASTLE + ":edit_name:" + id),
                    KeyboardFactory.btn("\uD83D\uDCDD Описание", Actions.CASTLE + ":edit_desc:" + id),
                    KeyboardFactory.btn("\uD83D\uDC64 Автор", Actions.CASTLE + ":edit_author:" + id),
                    KeyboardFactory.btn("\uD83D\uDCC5 Год осн.", Actions.CASTLE + ":edit_built:" + id),
                    KeyboardFactory.btn("\uD83D\uDCA5 Год разр.", Actions.CASTLE + ":edit_destroyed:" + id),
                    KeyboardFactory.btn("\uD83D\uDCCF Высота", Actions.CASTLE + ":edit_height:" + id),
                    KeyboardFactory.btn("\uD83E\uDDF1 Материал", Actions.CASTLE + ":edit_material:" + id)), 2);
            SendMessage s = m(chatId, "\u2712\uFE0F Замок ID:" + id + " — что меняем?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage editTextField(Long chatId, CallbackData cb, String field) {
        Long id = cb.argAsLong(0);
        sm.start(chatId, (cid, input) -> saveEdit(cid, id, field, input));
        return DialogStateMachine.removeKeyboard(m(chatId, "\u2712\uFE0F Введи новое значение для \"" + field + "\":"));
    }

    private SendMessage editFkField(Long chatId, CallbackData cb, String field) {
        Long id = cb.argAsLong(0);
        if (cb.args().size() >= 2) {
            Long fkId = cb.argAsLong(1);
            return saveEdit(chatId, id, field, fkId.toString());
        }
        try {
            if ("author".equals(field)) {
                List<AuthorResponse> authors = api.getAuthors();
                if (authors.isEmpty()) return m(chatId, "\u26A0\uFE0F Нет авторов.");
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(authors, 0, 5, a -> a.getName() + " (ID:" + a.getId() + ")", a -> Actions.CASTLE + ":edit_author:" + id + ":" + a.getId(), Actions.CASTLE + ":edit_page_author:" + id);
                SendMessage s = m(chatId, "\uD83D\uDC64 Выбери нового автора:"); s.setReplyMarkup(kb); return s;
            } else {
                List<MaterialResponse> mats = api.getMaterials();
                if (mats.isEmpty()) return m(chatId, "\u26A0\uFE0F Нет материалов.");
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, 0, 5, x -> x.getName() + " (ID:" + x.getId() + ")", x -> Actions.CASTLE + ":edit_material:" + id + ":" + x.getId(), Actions.CASTLE + ":edit_page_material:" + id);
                SendMessage s = m(chatId, "\uD83E\uDDF1 Выбери новый материал:"); s.setReplyMarkup(kb); return s;
            }
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage editFkPage(Long chatId, CallbackData cb, String field) {
        Long id = cb.argAsLong(0);
        int pg = cb.args().size() >= 2 ? parseInt(cb.arg(1), 0) : 0;
        try {
            if ("author".equals(field)) {
                List<AuthorResponse> authors = api.getAuthors();
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(authors, pg, 5, a -> a.getName() + " (ID:" + a.getId() + ")", a -> Actions.CASTLE + ":edit_author:" + id + ":" + a.getId(), Actions.CASTLE + ":edit_page_author:" + id);
                SendMessage s = m(chatId, "\uD83D\uDC64 Выбери автора:"); s.setReplyMarkup(kb); return s;
            } else {
                List<MaterialResponse> mats = api.getMaterials();
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(mats, pg, 5, x -> x.getName() + " (ID:" + x.getId() + ")", x -> Actions.CASTLE + ":edit_material:" + id + ":" + x.getId(), Actions.CASTLE + ":edit_page_material:" + id);
                SendMessage s = m(chatId, "\uD83E\uDDF1 Выбери материал:"); s.setReplyMarkup(kb); return s;
            }
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage saveEdit(Long chatId, Long id, String field, String input) {
        try {
            CastleResponse c = api.getCastle(id);
            CastleRequest req = new CastleRequest(); req.setName(c.getName()); req.setDescription(c.getDescription());
            req.setAuthorId(c.getAuthor() != null ? c.getAuthor().getId() : null);
            req.setMaterialId(c.getMaterial() != null ? c.getMaterial().getId() : null);
            req.setBuiltYear(c.getBuiltYear()); req.setDestroyedYear(c.getDestroyedYear()); req.setHeightM(c.getHeightM());
            switch (field) { case "name" -> req.setName(input); case "desc" -> req.setDescription(input); case "author" -> req.setAuthorId(Long.parseLong(input)); case "material" -> req.setMaterialId(Long.parseLong(input)); case "built" -> req.setBuiltYear("-".equals(input) ? null : Integer.parseInt(input)); case "destroyed" -> req.setDestroyedYear("-".equals(input) ? null : Integer.parseInt(input)); case "height" -> req.setHeightM("-".equals(input) ? null : new BigDecimal(input)); }
            api.updateCastle(id, req);
            sm.cancel(chatId);
            return submenu(chatId);
        } catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    // ---- Delete ----
    private SendMessage pickDelete(Long chatId, CallbackData cb) {
        try {
            List<CastleResponse> list = api.getCastles();
            if (list.isEmpty()) return m(chatId, "\uD83C\uDFF0 Летописи пусты.");
            if (!cb.hasArgs() || cb.arg(0).startsWith("page")) {
                int page = cb.hasArgs() ? parseInt(cb.arg(0).replace("page:", ""), 0) : 0;
                InlineKeyboardMarkup kb = KeyboardFactory.paginated(list, page, 5, c -> c.getName() + " (ID:" + c.getId() + ")", c -> Actions.CASTLE + ":delete:" + c.getId(), Actions.CASTLE + ":delete:page");
                SendMessage s = m(chatId, "\uD83D\uDCA5 Выбери замок:"); s.setReplyMarkup(kb); return s;
            }
            Long id = cb.argAsLong(0);
            InlineKeyboardMarkup kb = KeyboardFactory.inlineGrid(List.of(KeyboardFactory.btn("\u2705 Да", Actions.CASTLE + ":confirm_delete:" + id), KeyboardFactory.btn("\u274C Нет", Actions.CANCEL)), 2);
            SendMessage s = m(chatId, "\u26A0\uFE0F Удалить замок ID:" + id + "?"); s.setReplyMarkup(kb); return s;
        } catch (Exception e) { return err(chatId); }
    }

    private SendMessage confirmDelete(Long chatId, CallbackData cb) {
        try { api.deleteCastle(cb.argAsLong(0)); return submenu(chatId); }
        catch (Exception e) { return m(chatId, errMsg(e)); }
    }

    // ---- Helpers ----
    private String fmt(CastleResponse c) {
        StringBuilder sb = new StringBuilder("\uD83C\uDFF0 " + c.getName() + " (ID:" + c.getId() + ")\n");
        if (c.getDescription() != null) sb.append("\uD83D\uDCDD ").append(c.getDescription()).append("\n");
        if (c.getAuthor() != null) sb.append("\uD83D\uDC64 ").append(c.getAuthor().getName()).append("\n");
        sb.append("\uD83D\uDCC5 ").append(c.getBuiltYear() != null ? c.getBuiltYear() : "?").append(" — ").append(c.getDestroyedYear() != null ? c.getDestroyedYear() : "н.в.").append("\n");
        if (c.getHeightM() != null) sb.append("\uD83D\uDCCF ").append(c.getHeightM()).append(" м\n");
        if (c.getMaterial() != null) sb.append("\uD83E\uDDF1 ").append(c.getMaterial().getName()).append("\n");
        return sb.toString();
    }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; } }
    /** Returns true if input is invalid for a numeric/optional field. '-' is always valid (skip). */
    private static boolean invalid(String s, boolean allowDash) {
        if (s == null || s.isBlank()) return true;
        if (allowDash && "-".equals(s.trim())) return false;
        try { new java.math.BigDecimal(s.trim()); return false; }
        catch (NumberFormatException e) { return true; }
    }
    private String errMsg(Exception e) { String msg = e.getMessage(); return "\u26A0\uFE0F " + (msg != null && msg.length() > 200 ? msg.substring(0, 200) : msg); }
    private SendMessage m(Long cid, String t) { return new SendMessage(cid.toString(), t); }
    private SendMessage err(Long cid) { return m(cid, "\u26A0\uFE0F Сервер замка не отвечает."); }
}
