package org.justiks.telegram.routers;

import org.justiks.telegram.Bot;
import org.justiks.telegram.fsm.FSM;
import org.justiks.telegram.fsm.states.RequestToWhitelistState;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * router of telegram message commands
 */
public class CommandRouter extends BaseRouter<Update> {

    // consts
    private final String CANCEL_STATE_MESSAGE = "Действие успешно отменено!";
    private final String START_COMMAND_MESSAGE = "Привет! Чтобы подать заявку в вайтлист используй /request!";
    private final String NEW_REQUEST_COMMAND_MESSAGE = "Привет! Напиши свой будущий ник в Minecraft";

    private CommandRouter() {
        addHandler(update -> update.getMessage().getText().equals("/start"), this::startCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/request"), this::requestCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/cancel"), this::clearCommandExecutor);
        addHandler(update -> update.getMessage().getText().equals("/get_gift"), this::getGiftCommandExecutor);
    }

    // command executors

    private void startCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), START_COMMAND_MESSAGE));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void clearCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        FSM.getInstance().clearState(userId);
        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), CANCEL_STATE_MESSAGE));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void requestCommandExecutor(Update update) {
        Long userId = update.getMessage().getFrom().getId();

        FSM.getInstance().addState(userId, RequestToWhitelistState.MINECRAFT_NICKNAME);

        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), NEW_REQUEST_COMMAND_MESSAGE));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void getGiftCommandExecutor(Update update) {}
}
