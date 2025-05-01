package org.justiks.telegram.routers;


import org.justiks.telegram.handlers.Handler;
import org.justiks.telegram.handlers.PredicateHandler;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


/**
 * Main router, use in Bot().consume() only!
 * Singleton
 */
public class MainRouter implements Router<Update> {

    /**
     * instance
     */
    private static final MainRouter INSTANCE = new MainRouter();

    /**
     * singleton pattern
     * @return router instance
     */
    public static MainRouter getInstance() {
        return INSTANCE;
    }

    /**
     * constructor in singleton is private
     * initialize handlers
     */
    private MainRouter() {
        MessagesRouter<Update> messagesRouter = new MessagesRouter<>();
        addHandler(Update::hasMessage, messagesRouter::route);

        CallbackQueryRouter<Update> callbackQueryRouter = new CallbackQueryRouter<>();
        addHandler(Update::hasCallbackQuery, callbackQueryRouter::route);
    }

    /**
     * iter it, for call all handlers in router
     */
    private final List<Handler<Update>> handlers = new ArrayList<>();

    /**
     * add handler to handlers list
     * @param predicate predicate object
     * @param handler handler object
     */
    private void addHandler(Predicate<Update> predicate, Handler<Update> handler) {
        handlers.add(new PredicateHandler<Update>(predicate, handler));
    }

    @Override
    public void route(Update update) {
        for (Handler<Update> handler : handlers) {
            handler.handle(update);
        }
    }
}
