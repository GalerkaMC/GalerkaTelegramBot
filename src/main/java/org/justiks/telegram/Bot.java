package org.justiks.telegram;

import org.justiks.telegram.routers.MainRouter;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;


/**
 * Singleton
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    /**
     * Singleton bot instance
     */
    private static final Bot instance = new Bot();

    /**
     * Instance getter
     * @return Bot instance
     */
    public static Bot getInstance() {
        return instance;
    }

    /**
     * Singleton. Constructor is private
     */
    private Bot() {}

    /**
     * Consume update from telegram
     * @param update update from telegram API
     */
    @Override
    public void consume(Update update) {
        MainRouter.getInstance().route(update);
    }

    /**
     * Bot run method
     * @param token telegram bot token
     */
    public void run(String token) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(token, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
