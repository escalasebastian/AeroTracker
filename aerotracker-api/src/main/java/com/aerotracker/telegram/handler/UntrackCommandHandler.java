package com.aerotracker.telegram.handler;

import com.aerotracker.entity.Subscription;
import com.aerotracker.service.SubscriptionService;
import com.aerotracker.telegram.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles the "/untrack <number>" command to cancel an active alert.
 * Translates the user's 1-based visual list index into the internal database ID.
 */
@Component
public class UntrackCommandHandler implements TelegramCommandHandler {

    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;

    public UntrackCommandHandler(SubscriptionService subscriptionService, MessageSource messageSource) {
        this.subscriptionService = subscriptionService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return "/untrack".equalsIgnoreCase(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        String[] tokens = context.tokens();

        if (tokens.length != 2) {
            return messageSource.getMessage("telegram.untrack.invalid-format", null, context.locale());
        }

        try {
            int displayIndex = Integer.parseInt(tokens[1]);
            List<Subscription> activeSubs = subscriptionService.getActiveSubscriptions(context.chatId());

            if (displayIndex < 1 || displayIndex > activeSubs.size()) {
                return messageSource.getMessage("telegram.untrack.not-found", new Object[]{displayIndex}, context.locale());
            }

            Subscription targetSub = activeSubs.get(displayIndex - 1);
            boolean success = subscriptionService.untrackFlight(context.chatId(), targetSub.getId());

            if (success) {
                return messageSource.getMessage("telegram.untrack.success", new Object[]{displayIndex}, context.locale());
            } else {
                return messageSource.getMessage("telegram.untrack.not-found", new Object[]{displayIndex}, context.locale());
            }

        } catch (NumberFormatException e) {
            return messageSource.getMessage("telegram.untrack.invalid-format", null, context.locale());
        } catch (Exception e) {
            return messageSource.getMessage("telegram.error.unexpected", null, context.locale());
        }
    }
}