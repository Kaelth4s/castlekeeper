package org.kaelth4s.castlekeeper.bot.callback;

import java.util.List;

/**
 * Typed callback data parser. Format: prefix:action:args...
 * Replaces fragile string-splitting with structured access.
 *
 * <pre>{@code
 * CallbackData cb = CallbackData.parse("castle:edit:1");
 * cb.prefix()  → "castle"
 * cb.action()  → "edit"
 * cb.args()    → ["1"]
 * cb.arg(0)    → "1"
 * }</pre>
 */
public record CallbackData(String prefix, String action, List<String> args) {

    public static CallbackData parse(String data) {
        if (data == null || data.isEmpty()) return new CallbackData("", "", List.of());
        String[] parts = data.split(":", -1);
        if (parts.length == 1) return new CallbackData(parts[0], "", List.of());
        String prefix = parts[0];
        String action = parts[1];
        List<String> args = parts.length > 2 ? List.of(java.util.Arrays.copyOfRange(parts, 2, parts.length)) : List.of();
        return new CallbackData(prefix, action, args);
    }

    public String arg(int index) { return args.size() > index ? args.get(index) : ""; }
    public long argAsLong(int index) { return Long.parseLong(arg(index)); }
    public int argAsInt(int index) { return Integer.parseInt(arg(index)); }
    public boolean hasArgs() { return !args.isEmpty(); }
    public boolean actionEquals(String candidate) { return action.equals(candidate); }
    public boolean prefixEquals(String candidate) { return prefix.equals(candidate); }
}
