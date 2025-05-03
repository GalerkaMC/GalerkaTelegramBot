package org.justiks.telegram.fsm.states;

import org.jetbrains.annotations.Nullable;

/**
 * All statesgroups enums should implement it
 */
public interface State {
    /**
     * Get next
     * @return null or next value
     */
    @Nullable State getNext();
}


