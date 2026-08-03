package com.aerotracker.telegram.handler;

import com.aerotracker.telegram.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements TelegramCommandHandler {

    private final MessageSource messageSource;

    public StartCommandHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return "/start".equals(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        return messageSource.getMessage("telegram.start.help", null, context.locale());
    }
}
