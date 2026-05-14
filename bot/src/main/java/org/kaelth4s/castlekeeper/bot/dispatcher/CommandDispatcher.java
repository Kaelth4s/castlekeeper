package org.kaelth4s.castlekeeper.bot.dispatcher;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

@Component
public class CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    private final StartHandler startHandler;
    private final MenuHandler menuHandler;
    private final CastleHandler castleHandler;
    private final AuthorHandler authorHandler;
    private final AuthorTypeHandler authorTypeHandler;
    private final MaterialHandler materialHandler;
    private final ReconstructionHandler reconstructionHandler;
    private final DialogStateMachine sm;
    private final MeterRegistry meterRegistry;
    private final io.micrometer.core.instrument.Counter commandsTotal;
    private final io.micrometer.core.instrument.Counter errorsTotal;

    private static final Map<String, String> REPLY_BUTTONS = Map.ofEntries(
        Map.entry("\uD83D\uDEAA Войти в замок", "menu"),
        Map.entry("\uD83D\uDCDC Список замков", "castle:list"),
        Map.entry("\uD83C\uDFF0 Замки", "castle:menu"),
        Map.entry("\uD83D\uDC65 Авторы", "author:menu"),
        Map.entry("\uD83C\uDFF7\uFE0F Титулы", "author_type:menu"),
        Map.entry("\uD83E\uDDF1 Материалы", "material:menu"),
        Map.entry("\uD83D\uDD28 Реконструкции", "reconstruction:menu"),
        Map.entry("\uD83D\uDEAA Выйти из замка", "exit"),
        Map.entry("\uD83D\uDCDC Все замки", "castle:list"), Map.entry("\uD83D\uDC41\uFE0F Выбрать замок", "castle:view"),
        Map.entry("\uD83C\uDFB2 Случайный замок", "castle:random"), Map.entry("\uD83C\uDFD7\uFE0F Добавить замок", "castle:add"),
        Map.entry("\u2712\uFE0F Изменить замок", "castle:edit"), Map.entry("\uD83D\uDCA5 Удалить замок", "castle:delete"),
        Map.entry("\uD83D\uDCDC Все авторы", "author:list"), Map.entry("\uD83D\uDC41\uFE0F Выбрать автора", "author:view"),
        Map.entry("\uD83C\uDFD7\uFE0F Добавить автора", "author:add"), Map.entry("\u2712\uFE0F Изменить автора", "author:edit"),
        Map.entry("\uD83D\uDCA5 Удалить автора", "author:delete"), Map.entry("\uD83D\uDCDC Все титулы", "author_type:list"),
        Map.entry("\uD83C\uDFD7\uFE0F Добавить титул", "author_type:add"), Map.entry("\u2712\uFE0F Изменить титул", "author_type:edit"),
        Map.entry("\uD83D\uDCA5 Удалить титул", "author_type:delete"), Map.entry("\uD83D\uDCDC Все материалы", "material:list"),
        Map.entry("\uD83D\uDC41\uFE0F Выбрать материал", "material:view"), Map.entry("\uD83C\uDFD7\uFE0F Добавить материал", "material:add"),
        Map.entry("\u2712\uFE0F Изменить материал", "material:edit"), Map.entry("\uD83D\uDCA5 Удалить материал", "material:delete"),
        Map.entry("\uD83D\uDCDC Все реконструкции", "reconstruction:list"), Map.entry("\uD83D\uDC41\uFE0F Выбрать реконстр.", "reconstruction:view"),
        Map.entry("\uD83C\uDFD7\uFE0F Добавить реконстр.", "reconstruction:add"), Map.entry("\u2712\uFE0F Изменить реконстр.", "reconstruction:edit"),
        Map.entry("\uD83D\uDCA5 Удалить реконстр.", "reconstruction:delete"), Map.entry("\u21A9\uFE0F Назад в меню", "menu")
    );

    public CommandDispatcher(StartHandler startHandler, MenuHandler menuHandler, CastleHandler castleHandler,
                             AuthorHandler authorHandler, AuthorTypeHandler authorTypeHandler,
                             MaterialHandler materialHandler, ReconstructionHandler reconstructionHandler,
                             DialogStateMachine sm, MeterRegistry meterRegistry) {
        this.startHandler = startHandler; this.menuHandler = menuHandler; this.castleHandler = castleHandler;
        this.authorHandler = authorHandler; this.authorTypeHandler = authorTypeHandler;
        this.materialHandler = materialHandler; this.reconstructionHandler = reconstructionHandler;
        this.sm = sm;
        this.meterRegistry = meterRegistry;
        this.commandsTotal = io.micrometer.core.instrument.Counter.builder("castlekeeper.commands.total")
                .description("Total commands").register(meterRegistry);
        this.errorsTotal = io.micrometer.core.instrument.Counter.builder("castlekeeper.errors.total")
                .description("Total errors").register(meterRegistry);
    }

    public SendMessage dispatch(Long chatId, String text) {
        MDC.put("chatId", chatId.toString());
        MDC.put("command", text);
        commandsTotal.increment();
        try {
            if ("/cancel".equals(text)) { sm.cancelAll(chatId); return handleMenuReturn(chatId, sm.getPrefix(chatId)); }

            SendMessage wizardReply = sm.next(chatId, text);
            if (wizardReply != null) {
                if (!(wizardReply.getReplyMarkup() instanceof InlineKeyboardMarkup)) {
                    wizardReply.setReplyMarkup(new ReplyKeyboardRemove(true));
                }
                return wizardReply;
            }

            SendMessage cmd = routeCommand(chatId, text);
            if (cmd != null) return cmd;

            String callbackKey = REPLY_BUTTONS.get(text);
            if (callbackKey != null) return handleCallback(chatId, callbackKey);

            return new SendMessage(chatId.toString(), "\u2694\uFE0F Неведомая команда. Открой /menu.");
        } catch (Exception e) {
            errorsTotal.increment();
            log.error("Dispatch error", e);
            return new SendMessage(chatId.toString(), "\u26A0\uFE0F Внутренняя ошибка.");
        } finally {
            MDC.remove("chatId");
            MDC.remove("command");
        }
    }

    private SendMessage routeCommand(Long chatId, String text) {
        return switch (text) {
            case "/start" -> startHandler.handle(chatId, text);
            case "/menu" -> { sm.cancel(chatId); yield menuHandler.handle(chatId, text); }
            case "/castles" -> castleHandler.list(chatId);
            case "/authors" -> authorHandler.list(chatId);
            case "/author_types" -> authorTypeHandler.list(chatId);
            case "/materials" -> materialHandler.list(chatId);
            case "/reconstructions" -> reconstructionHandler.list(chatId);
            default -> null;
        };
    }

    public SendMessage handleCallback(Long chatId, String data) {
        MDC.put("chatId", chatId.toString());
        MDC.put("callback", data);
        try {
            log.info("Callback: {}", data);
            CallbackData cb = CallbackData.parse(data);

            if (cb.prefixEquals(Actions.NOOP) || "noop".equals(data) || "ignore".equals(data)) return null;
            if (cb.prefixEquals(Actions.CANCEL)) { sm.cancel(chatId); return handleMenuReturn(chatId, sm.getPrefix(chatId)); }
            if (cb.prefixEquals(Actions.ENTER)) { sm.setPrefix(chatId, Actions.MENU); return menuHandler.handle(chatId, data); }
            if (cb.prefixEquals(Actions.MENU)) { sm.setPrefix(chatId, Actions.MENU); return menuHandler.handle(chatId, data); }
            if (cb.prefixEquals(Actions.EXIT)) { sm.setPrefix(chatId, "start"); return startHandler.handle(chatId, data); }

            sm.setPrefix(chatId, cb.prefix());
            return switch (cb.prefix()) {
                case Actions.CASTLE -> castleHandler.handleCallback(chatId, cb);
                case Actions.AUTHOR -> authorHandler.handleCallback(chatId, cb);
                case Actions.AUTHOR_TYPE -> authorTypeHandler.handleCallback(chatId, cb);
                case Actions.MATERIAL -> materialHandler.handleCallback(chatId, cb);
                case Actions.RECONSTRUCTION -> reconstructionHandler.handleCallback(chatId, cb);
                default -> new SendMessage(chatId.toString(), "Эта дверь ещё не открыта.");
            };
        } catch (Exception e) {
            errorsTotal.increment();
            log.error("Callback error", e);
            return new SendMessage(chatId.toString(), "\u26A0\uFE0F Ошибка обработки.");
        } finally {
            MDC.remove("chatId");
            MDC.remove("callback");
        }
    }

    private SendMessage handleMenuReturn(Long chatId, String prefix) {
        return switch (prefix) {
            case Actions.CASTLE -> castleHandler.handle(chatId, "menu");
            case Actions.AUTHOR -> authorHandler.handle(chatId, "menu");
            case Actions.AUTHOR_TYPE -> authorTypeHandler.handle(chatId, "menu");
            case Actions.MATERIAL -> materialHandler.handle(chatId, "menu");
            case Actions.RECONSTRUCTION -> reconstructionHandler.handle(chatId, "menu");
            default -> menuHandler.handle(chatId, "menu");
        };
    }
}
