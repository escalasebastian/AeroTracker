package com.aerotracker.telegram.handler;

import com.aerotracker.common.provider.FlightPriceProvider;
import com.aerotracker.common.provider.FlightPriceRequest;
import com.aerotracker.common.provider.FlightPriceResponse;
import com.aerotracker.telegram.TelegramCommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Handles the "/price" command, answering a one-off price query straight away.
 * Syntax:
 * - One-way:    /price ORIGIN DESTINATION YYYY-MM-DD
 * - Round-trip: /price ORIGIN DESTINATION YYYY-MM-DD YYYY-MM-DD
 * <p>
 * Answers from the simulated provider, so anyone may run it as often as they like without
 * touching the metered API that the tracking pipeline relies on.
 */
@Component
public class PriceCommandHandler implements TelegramCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(PriceCommandHandler.class);

    private static final String COMMAND = "/price";

    private final FlightPriceProvider flightPriceProvider;
    private final MessageSource messageSource;

    public PriceCommandHandler(FlightPriceProvider flightPriceProvider, MessageSource messageSource) {
        this.flightPriceProvider = flightPriceProvider;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return COMMAND.equalsIgnoreCase(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        List<String> tokens = List.of(context.tokens());

        // Either [/price, MAD, AMS, 2027-03-15] or the same plus a return date
        if (tokens.size() < 4 || tokens.size() > 5) {
            return messageSource.getMessage("telegram.price.invalid-format", null, context.locale());
        }

        String origin = tokens.get(1);
        String destination = tokens.get(2);

        Optional<String> airportError = RouteInputRules.checkAirports(origin, destination);
        if (airportError.isPresent()) {
            return messageSource.getMessage(airportError.get(), null, context.locale());
        }

        LocalDate departureDate;
        LocalDate returnDate = null;

        try {
            departureDate = LocalDate.parse(tokens.get(3));
            if (tokens.size() == 5) {
                returnDate = LocalDate.parse(tokens.get(4));
            }
        } catch (DateTimeParseException e) {
            return messageSource.getMessage("telegram.price.invalid-date", null, context.locale());
        }

        // Basic domain validation: return date cannot be earlier than departure date
        if (returnDate != null && returnDate.isBefore(departureDate)) {
            return messageSource.getMessage("telegram.error.invalid-date-range", null, context.locale());
        }

        if (departureDate.isBefore(LocalDate.now())) {
            return messageSource.getMessage("telegram.error.past-date", null, context.locale());
        }

        try {
            FlightPriceResponse response = flightPriceProvider.getFlightPrice(
                    new FlightPriceRequest(origin, destination, departureDate, returnDate));

            String returnLine = "";
            if (response.returnDate() != null) {
                returnLine = messageSource.getMessage(
                        "telegram.price.result.return-date",
                        new Object[]{response.returnDate().toString()},
                        context.locale());
            }

            return messageSource.getMessage(
                    "telegram.price.result",
                    new Object[]{
                            response.origin(),
                            response.destination(),
                            response.departureDate().toString(),
                            returnLine,
                            response.price().toString(),
                            response.currency()
                    },
                    context.locale());

        } catch (Exception e) {
            log.error("Failed to resolve price for {} -> {}: {}", origin, destination, e.getMessage(), e);
            return messageSource.getMessage("telegram.error.unexpected", null, context.locale());
        }
    }
}
