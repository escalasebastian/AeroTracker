package com.aerotracker.telegram;

import com.aerotracker.telegram.handler.TelegramCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class TelegramCommandDispatcher {
    private static final Logger log = LoggerFactory.getLogger(TelegramCommandDispatcher.class);

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
                logUsage(context.command().toLowerCase(Locale.ROOT), context);
                return handler.handle(context);
            }
        }

        // If no handler supports the command, return unknown command message in the user's language
        logUsage("unknown", context);
        return messageSource.getMessage("telegram.command.unknown", null, context.locale());
    }

    /**
     * Writes one line per command so CloudWatch Logs Insights can count distinct users per command.
     * Only the command name and the numeric Telegram id are logged: never the message text, which
     * can contain anything, and never the username. Unrecognized input is logged as "unknown" for
     * the same reason.
     */
    private void logUsage(String command, TelegramCommandContext context) {
        log.info("telegram_command command={} user={}", command, context.chatId());
    }
}

