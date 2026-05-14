package org.kaelth4s.castlekeeper.bot.dialog;

import java.util.Optional;

/**
 * Abstraction for dialog state persistence.
 * In-memory for single instance, Redis for horizontal scaling.
 */
public interface DialogStateRepository {
    void putStep(Long chatId, DialogStep step);
    Optional<DialogStep> getStep(Long chatId);
    void removeStep(Long chatId);

    void putPrefix(Long chatId, String prefix);
    Optional<String> getPrefix(Long chatId);
    void removePrefix(Long chatId);
}
