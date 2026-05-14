package org.kaelth4s.castlekeeper.bot.dialog;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import static org.assertj.core.api.Assertions.assertThat;

class DialogStateMachineTest {

    private final DialogStateMachine sm = new DialogStateMachine(new InMemoryDialogStateRepository());

    @Test
    void shouldReturnNullWhenNoDialogActive() {
        assertThat(sm.next(1L, "hello")).isNull();
    }

    @Test
    void shouldProcessDialogStepAndClearState() {
        sm.start(1L, (cid, input) -> new SendMessage(cid.toString(), input.toUpperCase()));

        SendMessage result = sm.next(1L, "hello");

        assertThat(result.getText()).isEqualTo("HELLO");
        assertThat(sm.next(1L, "more")).isNull(); // state cleared
    }

    @Test
    void shouldRetryWhenStepReRegisters() {
        DialogStep[] holder = new DialogStep[1];
        holder[0] = (cid, input) -> {
            if (input.length() < 3) {
                sm.start(cid, holder[0]);
                return new SendMessage(cid.toString(), "too short");
            }
            return new SendMessage(cid.toString(), input);
        };
        sm.start(1L, holder[0]);

        SendMessage r1 = sm.next(1L, "hi");
        assertThat(r1.getText()).isEqualTo("too short");
        assertThat(sm.next(1L, "hello")).isNotNull();
        assertThat(sm.next(1L, "again")).isNull();
    }

    @Test
    void shouldCancelState() {
        sm.start(1L, (cid, input) -> new SendMessage(cid.toString(), input));
        sm.cancel(1L);
        assertThat(sm.next(1L, "hello")).isNull();
    }

    @Test
    void shouldCancelAllStateIncludingPrefix() {
        sm.setPrefix(1L, "castle");
        sm.start(1L, (cid, input) -> new SendMessage(cid.toString(), input));
        sm.cancelAll(1L);
        assertThat(sm.getPrefix(1L)).isEqualTo("menu"); // reset to default
        assertThat(sm.next(1L, "test")).isNull();
    }

    @Test
    void removeKeyboardShouldSetReplyKeyboardRemove() {
        SendMessage msg = new SendMessage("1", "test");
        DialogStateMachine.removeKeyboard(msg);
        assertThat(msg.getReplyMarkup()).isInstanceOf(ReplyKeyboardRemove.class);
    }
}
