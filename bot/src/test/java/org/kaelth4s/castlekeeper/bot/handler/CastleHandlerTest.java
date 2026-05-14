package org.kaelth4s.castlekeeper.bot.handler;

import org.junit.jupiter.api.Test;
import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.dialog.InMemoryDialogStateRepository;
import org.kaelth4s.castlekeeper.dto.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CastleHandlerTest {

    private final CastleKeeperApiClient api = mock(CastleKeeperApiClient.class);
    private final DialogStateMachine sm = spy(new DialogStateMachine(new InMemoryDialogStateRepository()));
    private final CastleHandler handler = new CastleHandler(api, sm);

    @Test
    void handleShouldReturnSubmenu() {
        SendMessage msg = handler.handle(1L, "menu");
        assertThat(msg.getText()).contains("\u041A\u0440\u044B\u043B\u043E \u0417\u0430\u043C\u043A\u043E\u0432");
        assertThat(msg.getReplyMarkup()).isInstanceOf(ReplyKeyboardMarkup.class);
    }

    @Test
    void listShouldReturnTextWhenNotEmpty() {
        CastleResponse c = new CastleResponse(); c.setId(1L); c.setName("Test");
        when(api.getCastles()).thenReturn(List.of(c));

        SendMessage msg = handler.list(1L);
        assertThat(msg.getText()).contains("Test").contains("ID:1");
        assertThat(msg.getReplyMarkup()).isNull();
    }

    @Test
    void randomShouldReturnFormattedCastle() {
        CastleResponse c = buildCastle(1L, "Neuschwanstein", 1869);
        when(api.getRandomCastle()).thenReturn(c);

        SendMessage msg = handler.handleCallback(1L, CallbackData.parse("castle:random"));
        assertThat(msg.getText()).contains("Neuschwanstein").contains("1869");
    }

    @Test
    void editShouldShowFieldPicker() {
        CastleResponse c = buildCastle(1L, "Test", null);
        when(api.getCastles()).thenReturn(List.of(c));

        SendMessage msg = handler.handleCallback(1L, CallbackData.parse("castle:edit:1"));
        assertThat(msg.getText()).contains("\u043C\u0435\u043D\u044F\u0435\u043C"); // "меняем"
    }

    @Test
    void addWizardShouldPromptForName() {
        SendMessage msg = handler.handleCallback(1L, CallbackData.parse("castle:add"));
        assertThat(msg.getText()).contains("\u043D\u0430\u0437\u0432\u0430\u043D\u0438\u0435"); // "название"
        verify(sm).start(eq(1L), any());
    }

    @Test
    void confirmDeleteShouldCallApiAndReturnSubmenu() {
        handler.handleCallback(1L, CallbackData.parse("castle:confirm_delete:1"));
        verify(api).deleteCastle(1L);
    }

    private CastleResponse buildCastle(Long id, String name, Integer year) {
        CastleResponse c = new CastleResponse();
        c.setId(id); c.setName(name);
        if (year != null) c.setBuiltYear(year);
        return c;
    }
}
