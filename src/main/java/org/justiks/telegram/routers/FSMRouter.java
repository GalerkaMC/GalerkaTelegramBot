package org.justiks.telegram.routers;

import org.justiks.telegram.Bot;
import org.justiks.telegram.fsm.FSM;
import org.justiks.telegram.fsm.UserState;
import org.justiks.telegram.fsm.states.RequestToWhitelistState;
import org.justiks.telegram.fsm.states.State;
import org.justiks.telegram.whitelist_request.WhitelistRequest;
import org.justiks.telegram.whitelist_request.WhitelistRequestsManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * router for finite state machine
 */
public class FSMRouter extends BaseRouter<Update> {

    // messages
    private static final String NO_TEXT = "Ты отправил сообщение без текста!";
    private static final String INCORRECT_NICKNAME_LENGTH = "Некорректная длина никнейма!\n\nДлина никнейма от 3 до 16 символов";
    private static final String ILLEGAL_NICKNAME_SYMBOLS = "Недопустимые символы в никнейме! Используй латинские буквы, цифры и нижние подчеркивания!";
    private static final String HAS_LICENCE_MESSAGE = "Отлично, у тебя есть Minecraft лицензия?";
    private static final String YES_I_HAVE_MINECRAFT_LICENSE = "Да, у меня есть лицензия";
    private static final String NO_I_HAVE_NOT_MINECRAFT_LICENSE = "Нет, у меня нет Minecraft лицензии";
    private static final String INVALID_ANSWER_USE_BUTTONS = "Для ответа на вопрос используй кнопки!";
    private static final String IS_CODE_FUTURE_STUDENT_MESSAGE = "Являешься ли/учился ли ты на курсе Код Будущего?";
    private static final String YES_I_STUDENT = "Да, я обучался";
    private static final String NO_I_NOT_STUDENT = "Нет, я не обучался";
    private static final String ABOUT_ME_MESSAGE = "Отлично! Последний шаг, расскажи не много о себе";

    /**
     * chat with requests
     */
    private static final long ADMINS_CHAT = 1;

    /**
     * constructor
     */
    public FSMRouter() {
        addHandler(
                update -> getStateById(update) == RequestToWhitelistState.MINECRAFT_NICKNAME,
                this::minecraftNicknameState
        );

        addHandler(
                update -> getStateById(update) == RequestToWhitelistState.HAS_MINECRAFT_LICENSE,
                this::hasMinecraftLicenseState
        );

        addHandler(
                update -> getStateById(update) == RequestToWhitelistState.IS_CODE_FUTURE_STUDENT,
                this::isCodeFutureStudentState
        );

        addHandler(
                update -> getStateById(update) == RequestToWhitelistState.ABOUT_ME,
                this::aboutMeState
        );

    }

    /**
     * get state from fsm by id
     * uses in FSMRouter constructor to simplify
     *
     * @param update telegram update
     * @return State
     */
    private State getStateById(Update update) {
        return FSM.getInstance().getState(update.getMessage().getFrom().getId()).currentState;
    }

    /**
     * Answer to minecraft nickname state
     * Validate nickname and write to UserState
     *
     * @param update telegram update
     */
    private void minecraftNicknameState(Update update) {

        String nickname = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();

        try {
            // check message has text
            if (nickname == null) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), NO_TEXT));
                return;
            }

            // Check length
            int length = nickname.length();
            if (length < 3 || length > 16) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), INCORRECT_NICKNAME_LENGTH));
                return;
            }

            // check illegal symbols
            if (!nickname.matches("^[a-zA-Z0-9_]+$")) {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), ILLEGAL_NICKNAME_SYMBOLS));
                return;
            }

            // else request minecraft license exists

            // create keyboard
            KeyboardButton yesButton = new KeyboardButton(YES_I_HAVE_MINECRAFT_LICENSE);
            KeyboardButton noButton = new KeyboardButton(NO_I_HAVE_NOT_MINECRAFT_LICENSE);
            KeyboardRow keyboardRow = new KeyboardRow();

            keyboardRow.add(yesButton);
            keyboardRow.add(noButton);

            ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> keyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
            keyboardMarkupBuilder.keyboardRow(keyboardRow);


            // send message
            FSM.getInstance().getState(userId).setValueForCurrentState(nickname);

            SendMessage message = new SendMessage(userId.toString(), HAS_LICENCE_MESSAGE);
            message.setReplyMarkup(keyboardMarkupBuilder.build());
            Bot.getInstance().getTelegramClient().executeAsync(message);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * answer to request has minecraft license
     * answer with keyboard buttons, validate answer. Use YES_I_HAVE_MINECRAFT_LICENSE and NO_I_HAVE_NOT_MINECRAFT_LICENSE
     *
     * @param update telegram update
     */
    private void hasMinecraftLicenseState(Update update) {
        String answer = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();

        try {


            // if it has license
            if (answer.equals(YES_I_HAVE_MINECRAFT_LICENSE)) {
                FSM.getInstance().getState(userId).setValueForCurrentState(true);

                // if it hasn't license
            } else if (answer.equals(NO_I_HAVE_NOT_MINECRAFT_LICENSE)) {
                FSM.getInstance().getState(userId).setValueForCurrentState(false);

                // if answer is no valid
            } else {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), INVALID_ANSWER_USE_BUTTONS));
                return;
            }

            // if message is valid, send next step

            // create keyboard
            KeyboardButton yesButton = new KeyboardButton(YES_I_STUDENT);
            KeyboardButton noButton = new KeyboardButton(NO_I_NOT_STUDENT);
            KeyboardRow keyboardRow = new KeyboardRow();

            keyboardRow.add(yesButton);
            keyboardRow.add(noButton);

            ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> keyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
            keyboardMarkupBuilder.keyboardRow(keyboardRow);

            // send message
            SendMessage message = new SendMessage(userId.toString(), IS_CODE_FUTURE_STUDENT_MESSAGE);
            message.setReplyMarkup(keyboardMarkupBuilder.build());
            Bot.getInstance().getTelegramClient().executeAsync(message);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Answer to reuqest isCodeFutureStudent
     * answer with keyboard buttons, validate answer. Use YES_I_STUDENT and NO_I_NOT_STUDENT
     *
     * @param update telegram update
     */
    private void isCodeFutureStudentState(Update update) {
        String answer = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();

        try {
            // if is student
            if (answer.equals(YES_I_STUDENT)) {
                FSM.getInstance().getState(userId).setValueForCurrentState(false);
            }

            // if isn't student
            else if (answer.equals(NO_I_NOT_STUDENT)) {
                FSM.getInstance().getState(userId).setValueForCurrentState(false);
            }

            // if no valid message
            else {
                Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), INVALID_ANSWER_USE_BUTTONS));
                return;
            }

            // if message is valid

            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), ABOUT_ME_MESSAGE));

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * About me state handler. End of RequestToWhitelistState
     *
     * @param update telegram update
     */
    private void aboutMeState(Update update) {
        String message = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();

        UserState userState = FSM.getInstance().getState(userId);
        userState.setValueForCurrentState(message);

        // create WhitelistRequest and send request to admins chat
        WhitelistRequest whitelistRequest = new WhitelistRequest(update, userState);
        WhitelistRequestsManager.getInstance().add(whitelistRequest);
        whitelistRequest.sendRequestToAdminsChat();

        try {
            Bot.getInstance().getTelegramClient().executeAsync(new SendMessage(userId.toString(), "Отлично, заявка успешно отправлена! Ожидайте ответа"));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
