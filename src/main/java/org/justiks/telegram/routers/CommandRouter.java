package org.justiks.telegram.routers;

import org.jetbrains.annotations.Nullable;
import org.justiks.telegram.Bot;
import org.justiks.telegram.fsm.FSM;
import org.justiks.telegram.fsm.states.RequestToWhitelistState;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;


/**
 * router of telegram message commands
 */
public class CommandRouter extends BaseRouter<Update> {

    // consts
    private static final String CANCEL_STATE_MESSAGE = "Действие успешно отменено!";
    private static final String START_COMMAND_MESSAGE = "Привет! Чтобы подать заявку в вайтлист используй /request!";
    private static final String NEW_REQUEST_COMMAND_MESSAGE = "Привет! Напиши свой будущий ник в Minecraft";
    private static final String REQUEST_ALREADY_SEND = "Ты уже отправлял заявку на попадание в белый список, можно отправить только одну. Если не согласен - @Justiks";
    private static final String GIFT_ALREADY_GET = "Ты уже получал подарок! Если не согласен - @Justiks";

    private static final String DATABASE_PATH = "jdbc:sqlite:database.db";


    // TODO: Fix commands with args
    public CommandRouter() {
        addHandler(update -> update.getMessage().getText().equals("/start"), this::startCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/request"), this::requestCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/cancel"), this::clearCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/get_gift"), this::getGiftCommandExecutor);
    }

    // command executors

    /**
     * /start command executor
     * @param update telegram update object
     */
    private void startCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        try {

            // attempt to add user to database
            try (Connection connection = DriverManager.getConnection(DATABASE_PATH)) {
                String request = "INSERT OR IGNORE INTO users VALUES (?)";
                PreparedStatement statement = connection.prepareStatement(request);
                statement.setLong(1, userId);
                statement.execute();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // send message
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), START_COMMAND_MESSAGE));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * /clear command executor
     * @param update telegram update object
     */
    private void clearCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        FSM.getInstance().clearState(userId);

        // send message
        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), CANCEL_STATE_MESSAGE));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * /request command executor
     * @param update telegram update object
     */
    private void requestCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();

        // false if request by this player no exists, else true
        boolean requestExists = false;

        // check users exists in who_already_request table
        try (Connection connection = DriverManager.getConnection(Bot.DATABASE_PATH)) {
            String request = "SELECT user_id FROM who_already_request WHERE user_id = ?";

            PreparedStatement statement = connection.prepareStatement(request);
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            requestExists = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            // if request has already been sent - cancel action
            if (!requestExists) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), REQUEST_ALREADY_SEND));
                return;
            }

            // start request form
            FSM.getInstance().addState(userId, RequestToWhitelistState.MINECRAFT_NICKNAME);
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), NEW_REQUEST_COMMAND_MESSAGE));

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * /get_gift command executor
     * @param update telegram update object
     */
    private void getGiftCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();

        // false if request by this player no exists, else true
        boolean giftAlreadyGet = false;

        // check users exists in giftedUsers table
        try (Connection connection = DriverManager.getConnection(Bot.DATABASE_PATH)) {
            String request = "SELECT user_id FROM gifted_users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            giftAlreadyGet = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try {
            // if gift already get - cancel
            if (!giftAlreadyGet) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), GIFT_ALREADY_GET));
                return;
            }

            // TODO: get gift logic

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
