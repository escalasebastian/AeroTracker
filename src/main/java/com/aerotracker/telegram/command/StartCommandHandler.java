package com.aerotracker.telegram.command;

import com.aerotracker.telegram.TelegramCommandHandler;
import com.aerotracker.telegram.dto.TelegramCommandContext;
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
