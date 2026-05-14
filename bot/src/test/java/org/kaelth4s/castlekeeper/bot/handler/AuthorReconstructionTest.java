package org.kaelth4s.castlekeeper.bot.handler;

import org.junit.jupiter.api.Test;
import org.kaelth4s.castlekeeper.bot.callback.CallbackData;
import org.kaelth4s.castlekeeper.bot.client.CastleKeeperApiClient;
import org.kaelth4s.castlekeeper.bot.dialog.DialogStateMachine;
import org.kaelth4s.castlekeeper.bot.dialog.InMemoryDialogStateRepository;
import org.kaelth4s.castlekeeper.dto.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorHandlerTest {

    private final CastleKeeperApiClient api = mock(CastleKeeperApiClient.class);
    private final DialogStateMachine sm = spy(new DialogStateMachine(new InMemoryDialogStateRepository()));
    private final AuthorHandler handler = new AuthorHandler(api, sm);

    @Test
    void handleShouldReturnSubmenu() {
        SendMessage msg = handler.handle(1L, "menu");
        assertThat(msg.getText()).contains("\u0441\u043A\u0440\u0438\u043F\u0442\u043E\u0440\u0438\u0439");
        assertThat(msg.getReplyMarkup()).isInstanceOf(ReplyKeyboardMarkup.class);
    }

    @Test
    void listShouldShowAuthors() {
        when(api.getAuthors()).thenReturn(List.of(new AuthorResponse(1L, "\u0413\u0435\u043D\u0440\u0438\u0445", null)));
        SendMessage msg = handler.list(1L);
        assertThat(msg.getText()).contains("\u0413\u0435\u043D\u0440\u0438\u0445").contains("ID:1");
    }

    @Test
    void confirmDeleteShouldCallApi() {
        handler.handleCallback(1L, CallbackData.parse("author:confirm_delete:5"));
        verify(api).deleteAuthor(5L);
    }

    @Test
    void addWizardShouldPromptForName() {
        SendMessage msg = handler.handleCallback(1L, CallbackData.parse("author:add"));
        assertThat(msg.getText()).contains("\u0438\u043C\u044F");
        verify(sm).start(eq(1L), any());
    }
}

class AuthorTypeHandlerTest {

    private final CastleKeeperApiClient api = mock(CastleKeeperApiClient.class);
    private final DialogStateMachine sm = spy(new DialogStateMachine(new InMemoryDialogStateRepository()));
    private final AuthorTypeHandler handler = new AuthorTypeHandler(api, sm);

    @Test
    void handleShouldReturnSubmenu() {
        SendMessage msg = handler.handle(1L, "menu");
        assertThat(msg.getText()).contains("\u0417\u0430\u043B \u0422\u0438\u0442\u0443\u043B\u043E\u0432");
    }

    @Test
    void listShouldShowTypes() {
        when(api.getAuthorTypes()).thenReturn(List.of(new AuthorTypeResponse(1L, "\u0421\u0442\u0440\u043E\u0438\u0442\u0435\u043B\u044C", null)));
        SendMessage msg = handler.list(1L);
        assertThat(msg.getText()).contains("\u0421\u0442\u0440\u043E\u0438\u0442\u0435\u043B\u044C");
    }

    @Test
    void confirmDeleteShouldCallApi() {
        handler.handleCallback(1L, CallbackData.parse("author_type:confirm_delete:3"));
        verify(api).deleteAuthorType(3L);
    }
}

class ReconstructionHandlerTest {

    private final CastleKeeperApiClient api = mock(CastleKeeperApiClient.class);
    private final DialogStateMachine sm = spy(new DialogStateMachine(new InMemoryDialogStateRepository()));
    private final ReconstructionHandler handler = new ReconstructionHandler(api, sm);

    @Test
    void handleShouldReturnSubmenu() {
        SendMessage msg = handler.handle(1L, "menu");
        assertThat(msg.getText()).isNotEmpty();
    }

    @Test
    void listShouldShowReconstructions() {
        ReconstructionResponse r = new ReconstructionResponse(); r.setId(1L); r.setReconstructionYear(2024);
        when(api.getReconstructions()).thenReturn(List.of(r));
        SendMessage msg = handler.list(1L);
        assertThat(msg.getText()).contains("2024");
    }

    @Test
    void confirmDeleteShouldCallApi() {
        handler.handleCallback(1L, CallbackData.parse("reconstruction:confirm_delete:2"));
        verify(api).deleteReconstruction(2L);
    }
}
