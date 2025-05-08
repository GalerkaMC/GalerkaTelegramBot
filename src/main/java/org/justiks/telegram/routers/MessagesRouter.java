package org.justiks.telegram.routers;

import org.justiks.telegram.fsm.FSM;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MessagesRouter extends BaseRouter<Update> {
    public MessagesRouter() {
        FSMRouter fsmRouter = new FSMRouter();
        CommandRouter commandRouter = new CommandRouter();

        addHandler(
                update -> update.getMessage().getText().startsWith("/"),
                commandRouter::route
        );

        addHandler(
                update -> FSM.getInstance().getState(update.getMessage().getFrom().getId()) != null,
                fsmRouter::route
        );
    }

}
