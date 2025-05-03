package org.justiks.telegram.fsm.states;

import org.jetbrains.annotations.Nullable;


/**
 * Request to whitelist states group
 */
public enum RequestToWhitelistState implements State {
    MINECRAFT_NICKNAME,
    HAS_MINECRAFT_LICENSE,
    IS_CODE_FUTURE_STUDENT,
    ABOUT_ME;

    /**
     * read docs in interface
     * @return next state
     */
    @Nullable
    @Override
    public State getNext() {
        return ordinal() < values().length - 1 ? values()[ordinal() + 1] : null;
    }
}
