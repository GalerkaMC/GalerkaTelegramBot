package org.justiks.telegram.handlers;


import java.util.function.Predicate;

/**
 * handler with predicate
 * @param <T>
 */
public class PredicateHandler <T> implements Handler<T> {
    /**
     * Predicate for handler
     */
    private final Predicate<T> predicate;

    /**
     * next handler in chain
     */
    private final Handler<T> handler;

    public PredicateHandler(Predicate<T> predicate, Handler<T> handler) {
        this.predicate = predicate;
        this.handler = handler;
    }

    @Override
    public void handle(T update) {
        if (predicate.test(update)) {
            handler.handle(update);
        }
    }
}
