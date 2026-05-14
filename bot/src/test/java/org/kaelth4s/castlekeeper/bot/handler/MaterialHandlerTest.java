package org.kaelth4s.castlekeeper.bot.handler;

import org.junit.jupiter.api.Test;
import org.kaelth4s.castlekeeper.bot.callback.Actions;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.dialog.InMemoryDialogStateRepository;
import org.kaelth4s.castlekeeper.dto.MaterialRequest;
import org.kaelth4s.castlekeeper.dto.MaterialResponse;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Example test demonstrating the pattern for all handlers.
 * Mock the API client and DialogStateMachine — verify handler logic in isolation.
 */
class MaterialHandlerTest {

    private final CastleKeeperApiClient api = mock(CastleKeeperApiClient.class);
    private final DialogStateMachine sm = mock(DialogStateMachine.class);
    private final MaterialHandler handler = new MaterialHandler(api, sm);

    @Test
    void listShouldReturnTextWhenNotEmpty() {
        when(api.getMaterials()).thenReturn(List.of(new MaterialResponse(1L, "\u0413\u0440\u0430\u043D\u0438\u0442")));

        SendMessage msg = handler.list(1L);

        assertThat(msg.getText()).contains("\u0413\u0440\u0430\u043D\u0438\u0442");
        assertThat(msg.getText()).contains("(ID:1)");
        assertThat(msg.getReplyMarkup()).isNull();
    }

    @Test
    void listShouldIndicateEmptyCollection() {
        when(api.getMaterials()).thenReturn(List.of());

        SendMessage msg = handler.list(1L);

        assertThat(msg.getText()).contains("\u043F\u0443\u0441\u0442\u0430");
    }

    @Test
    void callbackMenuShouldReturnSubmenu() {
        CallbackData cb = CallbackData.parse(Actions.MATERIAL + ":" + Actions.MENU);

        SendMessage msg = handler.handleCallback(1L, cb);

        assertThat(msg.getText()).contains("\u041A\u0440\u044B\u043B\u043E \u041C\u0430\u0442\u0435\u0440\u0438\u0430\u043B\u043E\u0432");
        assertThat(msg.getReplyMarkup()).isEqualTo(MaterialHandler.SUBMENU);
    }

    @Test
    void addWizardShouldStartDialogState() {
        SendMessage msg = handler.handleCallback(1L, CallbackData.parse("material:add"));

        assertThat(msg.getText()).contains("\u043D\u0430\u0437\u0432\u0430\u043D\u0438\u0435");
        verify(sm).start(eq(1L), any());
    }

    @Test
    void confirmDeleteShouldReturnSubmenu() {
        CallbackData cb = CallbackData.parse("material:confirm_delete:1");

        SendMessage msg = handler.handleCallback(1L, cb);

        assertThat(msg.getText()).contains("\u041A\u0440\u044B\u043B\u043E \u041C\u0430\u0442\u0435\u0440\u0438\u0430\u043B\u043E\u0432");
        verify(api).deleteMaterial(1L);
    }
}
