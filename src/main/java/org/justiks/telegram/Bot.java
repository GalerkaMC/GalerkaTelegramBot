package org.justiks.telegram;

import org.justiks.telegram.routers.MainRouter;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Singleton
 */
public class Bot implements LongPollingSingleThreadUpdateConsumer {

    // constants

    /**
     * jdbc sqlite3 database path. Use it in DriverManager.getConnection()
     */
    public static final String DATABASE_PATH = "jdbc:sqlite:database.db";


    /**
     * Singleton bot instance
     */
    private static final Bot instance = new Bot();

    /**
     * telegram client
     */
    private OkHttpTelegramClient telegramClient;

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
        try (Connection connection = DriverManager.getConnection(DATABASE_PATH)) {
            // requests

            // all bot users table
            String createUsersTableRequest =
                    "CREATE TABLE IF NOT EXISTS users (user_id INT PRIMARY KEY)";
            // nickname is primary key because search by nickname more often than by user_id
            String createLinkedAccounts =
                    "CREATE TABLE IF NOT EXISTS linked_users (user_id INT UNIQUE, nickname TEXT PRIMARY KEY, FOREIGN KEY(user_id) REFERENCES users(user_id))";
            // users who already get gift
            String giftedUsers = "CREATE TABLE IF NOT EXISTS gifted_users (user_id INT PRIMARY KEY, player TEXT UNIQUE, FOREIGN KEY (user_id) REFERENCES users(user_id))";

            // users who already request
            String whoAlreadyRequest = "CREATE TABLE IF NOT EXISTS who_already_request (user_id INT PRIMARY KEY, FOREIGN KEY (user_id) REFERENCES users(user_id))";

            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                // execute all statements
                stmt.executeUpdate(createUsersTableRequest);
                stmt.executeUpdate(createLinkedAccounts);
                stmt.executeUpdate(giftedUsers);
                stmt.executeUpdate(whoAlreadyRequest);
                connection.commit();

            } catch (SQLException e) {
                // cancel transaction
                connection.rollback();
                connection.setAutoCommit(true);
                throw e;
            }

            // reset autocommit
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        // attempt to create database tables
        createTables();

        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            telegramClient = new OkHttpTelegramClient(token);
            botsApplication.registerBot(token, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
