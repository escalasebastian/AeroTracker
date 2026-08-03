package com.aerotracker.telegram.handler;

import com.aerotracker.telegram.TelegramCommandContext;

public interface TelegramCommandHandler {
    /**
     * Determines if this handler can process the given command.
     *
     * @param command Command name (e.g. "/price" or "/start")
     * @return true if this handler supports the command, false otherwise
     */
    boolean supports(String command);

    /**
     * Executes the command using the data encapsulated in the context.
     *
     * @param context The command execution context
     * @return The formatted text message ready to be sent to the user
     */
    String handle(TelegramCommandContext context);
}

