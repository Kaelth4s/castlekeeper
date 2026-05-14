package org.kaelth4s.castlekeeper.bot.dialog;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDialogStateRepository implements DialogStateRepository {
    private final Map<Long, DialogStep> steps = new ConcurrentHashMap<>();
    private final Map<Long, String> prefixes = new ConcurrentHashMap<>();

    @Override public void putStep(Long chatId, DialogStep step) { steps.put(chatId, step); }
    @Override public Optional<DialogStep> getStep(Long chatId) { return Optional.ofNullable(steps.get(chatId)); }
    @Override public void removeStep(Long chatId) { steps.remove(chatId); }
    @Override public void putPrefix(Long chatId, String prefix) { prefixes.put(chatId, prefix); }
    @Override public Optional<String> getPrefix(Long chatId) { return Optional.ofNullable(prefixes.get(chatId)); }
    @Override public void removePrefix(Long chatId) { prefixes.remove(chatId); }
}
