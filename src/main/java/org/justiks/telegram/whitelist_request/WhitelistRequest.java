package org.justiks.telegram.whitelist_request;


import org.jetbrains.annotations.Nullable;
import org.justiks.telegram.Bot;
import org.justiks.telegram.External;
import org.justiks.telegram.fsm.UserState;
import org.justiks.telegram.fsm.states.RequestToWhitelistState;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;

/**
 * whitelist request class
 */
public class WhitelistRequest {

    // constants
    private static final String ADMINS_CHAT = "-1002590134862";

    // messages
    private static final String REQUEST_MESSAGE = "Новая заявка от игрока %s:\n\nЮзернейм в телеграм: @%s\nПолное имя в телеграм: %s\nMinecraft никнейм: %s\nЕсть лицензия: %s\nУчился на Коде будущего: %s\nНе много о себе: %s";
    private static final String REQUEST_WAS_REJECTED = "Ваша заявка на попадание в белый список была отклонена.\n\nЗа подробностями писать @Justiks";
    private static final String REQUEST_ACCEPTED = "Ваша заявка в whitelist была одобрена!\nОдноразовый токен регистрации - %s\n\nПриятной игры!";

    /**
     * player telegram user id
     */
    public final Long telegramUserId;

    /**
     * player telegram. If player hasn't username - null
     */
    @Nullable
    public final String telegramUsername;

    /**
     * player telegram first name + last name
     */
    public final String telegramFullName;

    /**
     * minecraft nickname
     */
    public String nickname;

    /**
     * if player has minecraft license - true, else false
     */
    public boolean hasMinecraftLicense;

    /**
     * if player is code future student - true, else false
     */
    public boolean isCodeFutureStudent;

    /**
     * about player
     */
    public String aboutMe;

    /**
     * Constructor
     *
     * @param update    last telegram Update object from last step
     * @param userState player UserState object
     */
    public WhitelistRequest(Update update, UserState userState) {

        User user = update.getMessage().getFrom();

        telegramUserId = user.getId();
        telegramUsername = user.getUserName();
        // get full telegram name (first_name + last_name)
        telegramFullName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");

        nickname = (String) userState.getValueByState(RequestToWhitelistState.MINECRAFT_NICKNAME);
        hasMinecraftLicense = (boolean) userState.getValueByState(RequestToWhitelistState.HAS_MINECRAFT_LICENSE);
        isCodeFutureStudent = (boolean) userState.getValueByState(RequestToWhitelistState.IS_CODE_FUTURE_STUDENT);
        aboutMe = (String) userState.getValueByState(RequestToWhitelistState.ABOUT_ME);
    }

    /**
     * send message with request to admins chat
     */
    public void sendRequestToAdminsChat() {
        // format message for admins with info
        String messageText = String.format(
                REQUEST_MESSAGE,
                nickname,
                telegramUsername,
                telegramFullName,
                nickname,
                hasMinecraftLicense ? "Да" : "Нет",
                isCodeFutureStudent ? "Да" : "Нет",
                aboutMe
        );

        // create inline keyboard
        InlineKeyboardButton acceptButton = new InlineKeyboardButton("Принять ✅");
        InlineKeyboardButton rejectButton = new InlineKeyboardButton("Отклонить ❌");

        // callback should be like command:userId:accept command:userId:reject. Example whitelist_add:12345:accept
        acceptButton.setCallbackData("whitelist_add:" + telegramUserId + ":accept");
        rejectButton.setCallbackData("whitelist_add:" + telegramUserId + ":reject");

        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
        inlineKeyboardRow.add(acceptButton);
        inlineKeyboardRow.add(rejectButton);

        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> inlineKeyboardMarkupBuilder = InlineKeyboardMarkup.builder();
        inlineKeyboardMarkupBuilder.keyboardRow(inlineKeyboardRow);

        // send message
        SendMessage message = new SendMessage(ADMINS_CHAT, messageText);
        message.setReplyMarkup(inlineKeyboardMarkupBuilder.build());

        try {
            Bot.getInstance().getTelegramClient().executeAsync(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * call it, if whitelist request is accepted
     * add to whitelist and write to database
     */
    public void acceptWhitelistRequest() {
        String token = External.addToWhitelist(nickname);

        // add to database linked
        try (Connection connection = DriverManager.getConnection(Bot.DATABASE_PATH)) {
            String request = "INSERT INTO linked_users (user_id, nickname) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setLong(1, telegramUserId);
            statement.setString(2, nickname);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // send message
        String message = String.format(REQUEST_ACCEPTED, token);
        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(telegramUserId.toString(), message));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * call it if whitelist request was rejected
     */
    public void rejectWhitelistRequest() {
        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(telegramUserId.toString(), REQUEST_WAS_REJECTED));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
