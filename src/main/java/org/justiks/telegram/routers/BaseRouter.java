package org.justiks.telegram.routers;

import org.justiks.telegram.handlers.Handler;
import org.justiks.telegram.handlers.PredicateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * all routers should extend this class
 * @param <T> type of object entering the router
 */
public abstract class BaseRouter<T> implements Router<T> {
    /**
     * iter it, for call all handlers in router
     */
    protected final List<PredicateHandler<T>> handlers = new ArrayList<>();

    /**
     * add handler to handlers list
     * @param predicate predicate object
     * @param handler handler object
     */
    protected void addHandler(Predicate<T> predicate, Handler<T> handler) {
        handlers.add(new PredicateHandler<>(predicate, handler));
    }

    @Override
    public void route(T t) {
        for (PredicateHandler<T> handler : handlers) {
            if (handler.testPredicate(t)) {
                handler.handle(t);
                return;
            }
        }
    }
}
