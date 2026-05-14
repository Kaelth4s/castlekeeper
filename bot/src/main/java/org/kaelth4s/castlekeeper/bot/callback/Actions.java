package org.kaelth4s.castlekeeper.bot.callback;

/**
 * Centralized constants for all callback prefixes and actions.
 * Eliminates "magic strings" scattered across handlers.
 */
public final class Actions {
    private Actions() {}

    // Prefixes
    public static final String CASTLE = "castle";
    public static final String AUTHOR = "author";
    public static final String AUTHOR_TYPE = "author_type";
    public static final String MATERIAL = "material";
    public static final String RECONSTRUCTION = "reconstruction";

    // Common actions (used across all entities)
    public static final String MENU = "menu";
    public static final String LIST = "list";
    public static final String VIEW = "view";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String CONFIRM_DELETE = "confirm_delete";
    public static final String RANDOM = "random";

    // Navigation
    public static final String ENTER = "enter";
    public static final String EXIT = "exit";
    public static final String CANCEL = "cancel";
    public static final String NOOP = "noop";
}
