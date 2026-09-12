package com.aerotracker.telegram.handler;

import com.aerotracker.entity.Subscription;
import com.aerotracker.exception.RouteLimitExceededException;
import com.aerotracker.exception.UserRouteLimitExceededException;
import com.aerotracker.service.SubscriptionService;
import com.aerotracker.telegram.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Handles the "/track" command to create or update flight tracking alerts.
 * Syntax:
 * - One-way:    /track ORIGIN DESTINATION YYYY-MM-DD TARGET_PRICE
 * - Round-trip: /track ORIGIN DESTINATION YYYY-MM-DD YYYY-MM-DD TARGET_PRICE
 */
@Component
public class TrackCommandHandler implements TelegramCommandHandler {

    private static final String COMMAND = "/track";

    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;

    public TrackCommandHandler(SubscriptionService subscriptionService, MessageSource messageSource) {
        this.subscriptionService = subscriptionService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return COMMAND.equalsIgnoreCase(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        List<String> tokens = List.of(context.tokens());

        // Must have at least 5 tokens: [/track, MAD, AMS, 2027-03-15, 150]
        if (tokens.size() < 5 || tokens.size() > 6) {
            return messageSource.getMessage("telegram.track.invalid-format", null, context.locale());
        }

        try {
            // Routes are stored upper-cased, so lookups must use the same form: otherwise
            // "/track mad ams" would miss an existing MAD-AMS route and try to insert a duplicate
            String origin = tokens.get(1).toUpperCase(Locale.ROOT);
            String destination = tokens.get(2).toUpperCase(Locale.ROOT);
            LocalDate departureDate = LocalDate.parse(tokens.get(3));
            LocalDate returnDate = null;
            BigDecimal targetPrice;

            if (tokens.size() == 6) {
                // Round-trip: [/track, MAD, AMS, 2027-03-15, 2027-03-22, 150]
                returnDate = LocalDate.parse(tokens.get(4));
                targetPrice = new BigDecimal(tokens.get(5));
            } else {
                // One-way: [/track, MAD, AMS, 2027-03-15, 150]
                targetPrice = new BigDecimal(tokens.get(4));
            }

            Optional<String> inputError = RouteInputRules.checkAirports(origin, destination);
            if (inputError.isEmpty()) {
                inputError = RouteInputRules.checkTargetPrice(targetPrice);
            }
            if (inputError.isPresent()) {
                return messageSource.getMessage(inputError.get(), null, context.locale());
            }

            // Basic domain validation: return date cannot be earlier than departure date
            if (returnDate != null && returnDate.isBefore(departureDate)) {
                return messageSource.getMessage("telegram.error.invalid-date-range", null, context.locale());
            }

            // A departed flight can never be priced, so tracking it would only waste provider calls
            if (departureDate.isBefore(LocalDate.now())) {
                return messageSource.getMessage("telegram.error.past-date", null, context.locale());
            }

            // Delegate ACID transaction and normalization to the domain service
            Subscription sub = subscriptionService.trackFlight(
                    context.chatId(),
                    context.username(),
                    origin,
                    destination,
                    departureDate,
                    returnDate,
                    targetPrice
            );

            // Format success response using i18n
            String returnLine = "";
            if (sub.getRoute().getReturnDate() != null) {
                returnLine = messageSource.getMessage(
                        "telegram.track.return-line",
                        new Object[]{sub.getRoute().getReturnDate().toString()},
                        context.locale()
                );
            }

            return messageSource.getMessage(
                    "telegram.track.success",
                    new Object[]{
                            sub.getRoute().getOrigin(),
                            sub.getRoute().getDestination(),
                            sub.getRoute().getDepartureDate().toString(),
                            returnLine,
                            sub.getTargetPrice().toString()
                    },
                    context.locale()
            );

        } catch (DateTimeParseException | NumberFormatException e) {
            return messageSource.getMessage("telegram.track.invalid-format", null, context.locale());
        } catch (UserRouteLimitExceededException e) {
            return messageSource.getMessage(
                    "telegram.track.user-limit-reached",
                    new Object[]{e.getLimit()},
                    context.locale());
        } catch (RouteLimitExceededException e) {
            return messageSource.getMessage(
                    "telegram.track.route-limit-reached",
                    new Object[]{e.getLimit()},
                    context.locale());
        } catch (Exception e) {
            return messageSource.getMessage("telegram.error.unexpected", null, context.locale());
        }
    }
}