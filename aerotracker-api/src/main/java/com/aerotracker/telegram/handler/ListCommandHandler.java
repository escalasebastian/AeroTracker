package com.aerotracker.telegram.handler;

import com.aerotracker.entity.Subscription;
import com.aerotracker.service.SubscriptionService;
import com.aerotracker.telegram.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles the "/list" command to display all active tracking alerts for the user.
 * Numbers the list starting from 1 for clean visual user experience (UX).
 */
@Component
public class ListCommandHandler implements TelegramCommandHandler {

    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;

    public ListCommandHandler(SubscriptionService subscriptionService, MessageSource messageSource) {
        this.subscriptionService = subscriptionService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return "/list".equalsIgnoreCase(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        List<Subscription> subscriptions = subscriptionService.getActiveSubscriptions(context.chatId());

        if (subscriptions.isEmpty()) {
            return messageSource.getMessage("telegram.list.empty", null, context.locale());
        }

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(messageSource.getMessage("telegram.list.header", null, context.locale()));

        for (int i = 0; i < subscriptions.size(); i++) {
            Subscription sub = subscriptions.get(i);
            int displayIndex = i + 1; // 1, 2, 3...

            String datesStr = sub.getRoute().getDepartureDate().toString();
            if (sub.getRoute().getReturnDate() != null) {
                datesStr += " / " + sub.getRoute().getReturnDate().toString();
            }

            responseBuilder.append(messageSource.getMessage(
                    "telegram.list.item",
                    new Object[]{
                            displayIndex,
                            sub.getRoute().getOrigin(),
                            sub.getRoute().getDestination(),
                            datesStr,
                            sub.getTargetPrice().toString()
                    },
                    context.locale()
            ));
        }

        return responseBuilder.toString();
    }
}