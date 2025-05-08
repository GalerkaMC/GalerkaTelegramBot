package org.justiks.telegram;

import okhttp3.OkHttpClient;
import org.justiks.telegram.routers.MainRouter;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
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
     * telegram client
     */
    private final OkHttpTelegramClient telegramClient = new OkHttpTelegramClient("6664274548:AAG2ouZjFypnzMXrgPk79U8qWCBxOtlUmaM");

    /**
     * Instance getter
     * @return Bot instance
     */
    public static Bot getInstance() {
        return instance;
    }

    /**
     * telegram client getter
     * @return telegram client
     */
    public OkHttpTelegramClient getTelegramClient() {
        return telegramClient;
    }

    /**
     * Singleton. Constructor is private
     */
    private Bot() {}

    /**
     * Create database tables if no exists
     */
    private void createTables() {
        // TODO: Создай таблицы
    }

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
