package org.justiks.telegram.routers;


import org.telegram.telegrambots.meta.api.objects.Update;


/**
 * Main router, use in Bot().consume() only!
 * Singleton
 */
public class MainRouter extends BaseRouter<Update> {

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
        MessagesRouter messagesRouter = new MessagesRouter();
        addHandler(Update::hasMessage, messagesRouter::route);

        CallbackQueryRouter callbackQueryRouter = new CallbackQueryRouter();
        addHandler(Update::hasCallbackQuery, callbackQueryRouter::route);
    }
}
