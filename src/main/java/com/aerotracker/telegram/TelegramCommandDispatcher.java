package com.aerotracker.telegram;

import com.aerotracker.telegram.dto.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramCommandDispatcher {
    private final List<TelegramCommandHandler> handlers;
    private final MessageSource messageSource;

    public TelegramCommandDispatcher(List<TelegramCommandHandler> handlers, MessageSource messageSource) {
        this.handlers = handlers;
        this.messageSource = messageSource;
    }

    /**
     * Receives the context of the user message and delegates to the appropriate handler.
     *
     * @param context The command context containing the text and user locale
     * @return Formatted response ready to be sent to the chat
     */
    public String dispatch(TelegramCommandContext context) {
        if (context.rawText() == null || context.rawText().isBlank()) {
            return messageSource.getMessage("telegram.command.empty", null, context.locale());
        }

        for (TelegramCommandHandler handler : handlers) {
            if (handler.supports(context.command())) {
                return handler.handle(context);
            }
        }

        // If no handler supports the command, return unknown command message in the user's language
        return messageSource.getMessage("telegram.command.unknown", null, context.locale());
    }
}

