package org.justiks.telegram.routers;

import org.justiks.telegram.Bot;
import org.justiks.telegram.External;
import org.justiks.telegram.fsm.FSM;
import org.justiks.telegram.fsm.states.RequestToWhitelistState;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;


/**
 * router of telegram message commands
 */
public class CommandRouter extends BaseRouter<Update> {

    // consts
    private static final String CANCEL_STATE_MESSAGE = "✅ Действие успешно отменено!";
    private static final String START_COMMAND_MESSAGE = "\uD83D\uDC4B Привет! Чтобы подать заявку в вайтлист используй /request!";
    private static final String NEW_REQUEST_COMMAND_MESSAGE = "Заполни форму. Напиши свой будущий ник в Minecraft";
    private static final String REQUEST_ALREADY_SEND = "❌ Ты уже отправлял заявку на попадание в белый список, можно отправить только одну. Если не согласен - @Justiks";
    private static final String GIFT_ALREADY_GET = "✅ Ты уже получал подарок! Если не согласен - @Justiks";
    private static final String ACCOUNT_ALREADY_LINKED = "❌ К этому телеграм аккаунту уже привязан другой Minecraft аккаунт!";
    private static final String INCORRECT_LOGIN_OR_PASSWORD = "❌ Некорректный никнейм или пароль! Если это не так - @Justiks";
    private static final String LINK_NOT_ENOUGH_ARGUMENTS = "❌ Недостаточно аргументов, используйте /link <nickname> <password>";
    private static final String SUCCESSFUL_LINKED = "✅ Аккаунт успешно привязан!";
    private static final String DATABASE_PATH = "jdbc:sqlite:database.db";
    private static final String FAIL_SUBSCRIBE_CHECK = "❌ Вы не являетесь подписчиком этого канала!";
    private static final String ACCOUNT_IS_NOT_LINKED = "❌ К этому телеграмм-аккаунту не привязан ни один Minecraft аккаунт. Для получения подарка привяжите аккаунт (Используйте команду /link или подайте заявку в whitelsit)";
    private static final String GIFT_SUCCESSFUL_RECEIVED = "✅ Ты успешно получил подарок!";

    /**
     * channel where need check player subscribe
     */
    private static final String CHECK_SUBSCRIBE_CHANNEL = "-1002382327240";



    public CommandRouter() {

        addHandler(update -> update.getMessage().getText().startsWith("/start"), this::startCommandExecutor);
        addHandler(update -> update.getMessage().getText().startsWith("/request"), this::requestCommandExecutor);
        addHandler(update -> update.getMessage().getText().startsWith("/cancel"), this::clearCommandExecutor);
        addHandler(update -> update.getMessage().getText().startsWith("/get_gift"), this::getGiftCommandExecutor);
        addHandler(update -> update.getMessage().getText().startsWith("/link"), this::linkCommandExecutor);
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
        boolean requestExists;

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
            if (requestExists) {
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

        // false if request by this player no exists,private static final String else true
        boolean giftAlreadyGet;

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
            if (giftAlreadyGet) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), GIFT_ALREADY_GET));
                return;
            }


            // check users exists in linked users
            boolean isLinkedAccount;
            String nickname;

            try (Connection connection = DriverManager.getConnection(Bot.DATABASE_PATH)) {
                String request = "SELECT user_id, nickname FROM linked_users WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(request);
                statement.setLong(1, userId);
                ResultSet resultSet = statement.executeQuery();
                isLinkedAccount = resultSet.next();
                nickname = resultSet.getString("nickname");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // if telegram account is not linked
            if (!isLinkedAccount) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), ACCOUNT_IS_NOT_LINKED));
                return;
            }

            // check subscribe
            ChatMember chatMember = Bot.getInstance().getTelegramClient().execute(new GetChatMember(CHECK_SUBSCRIBE_CHANNEL, userId));
            String status = chatMember.getStatus();

            // if player is not subscribed
            if (!(status.equals("member") || status.equals("administrator") || status.equals("creator"))) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), FAIL_SUBSCRIBE_CHECK));
                return;
            }

            boolean result = External.giveGift(nickname);

            if (result) {
                // add user to gifted_users table
                try (Connection connection = DriverManager.getConnection(DATABASE_PATH)) {
                    String request = "INSERT INTO gifted_users VALUES (?, ?)";
                    PreparedStatement statement = connection.prepareStatement(request);
                    statement.setLong(1, userId);
                    statement.setString(2, nickname);
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), GIFT_SUCCESSFUL_RECEIVED));
            }


        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO: add cooldown for this command
     * /link command executor
     * link minecraft account to telegram
     * this command have arguments, use /link <nickname> <password>
     * @param update telegram update
     */
    private void linkCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();

        try {

            // get args from message
            String[] split_message = update.getMessage().getText().split(" ");

            // check arguments amount
            if (split_message.length < 3) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), LINK_NOT_ENOUGH_ARGUMENTS));
                return;
            }

            // check telegram account, does it have a link to minecraft account
            try (Connection connection = DriverManager.getConnection(DATABASE_PATH)) {
                String request = "SELECT user_id FROM linked_users WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(request);
                statement.setLong(1, userId);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), ACCOUNT_ALREADY_LINKED));
                    return;
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            String nickname = split_message[1];
            String password = split_message[2];

            if (External.playerAuth(nickname, password)) {

                // Add player to linked_users table
                try (Connection connection = DriverManager.getConnection(DATABASE_PATH)) {
                    String request = "INSERT INTO linked_users VALUES (?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(request);
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setString(2, nickname);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), SUCCESSFUL_LINKED));

            } else {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), INCORRECT_LOGIN_OR_PASSWORD));
            }

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
