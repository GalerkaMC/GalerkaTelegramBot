package org.justiks.telegram.routers;

import org.justiks.telegram.Bot;
import org.justiks.telegram.whitelist_request.WhitelistRequest;
import org.justiks.telegram.whitelist_request.WhitelistRequestsManager;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * callback query router
 */
public class CallbackQueryRouter extends BaseRouter<Update> {

    // constants

    // messages
    public static final String REQUEST_SUCCESSFUL_REJECTED = "Заявка игрока %s успешно отклонена ❌";
    public static final String REQUEST_SUCCESSFUL_ACCEPTED = "Заявка игрока %s успешно принята ✅";

    public CallbackQueryRouter() {
        addHandler(update -> update.getCallbackQuery().getData().startsWith("whitelist_add"), this::whitelistAddCallbackQueryExecutor);
    }

    /**
     * whitelist_add callback query executor
     * @param update telegram update object
     */
    private void whitelistAddCallbackQueryExecutor(Update update) {
        String[] splitCallbackQueryData = update.getCallbackQuery().getData().split(":");

        Long userId = Long.parseLong(splitCallbackQueryData[1]);
        String command = splitCallbackQueryData[2];
        WhitelistRequest whitelistRequest = WhitelistRequestsManager.getInstance().get(userId);

        // reject request
        if (command.equals("reject")) {
            whitelistRequest.rejectWhitelistRequest();

            // create answer callback query
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
            answerCallbackQuery.setText(String.format(REQUEST_SUCCESSFUL_REJECTED, whitelistRequest.nickname));
            answerCallbackQuery.setShowAlert(false);

            try {
                Bot.getInstance().getTelegramClient().executeAsync(answerCallbackQuery);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        // accept request
        else if (command.equals("accept")) {
            whitelistRequest.acceptWhitelistRequest();

            // create answer callback query
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
            answerCallbackQuery.setText(String.format(REQUEST_SUCCESSFUL_ACCEPTED, whitelistRequest.nickname));
            answerCallbackQuery.setShowAlert(false);

            try {
                Bot.getInstance().getTelegramClient().executeAsync(answerCallbackQuery);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
