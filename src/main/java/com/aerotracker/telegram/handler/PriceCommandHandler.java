package com.aerotracker.telegram.handler;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import com.aerotracker.service.FlightPriceService;
import com.aerotracker.telegram.TelegramCommandContext;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class PriceCommandHandler implements TelegramCommandHandler {

    private final FlightPriceService priceService;
    private final MessageSource messageSource;

    public PriceCommandHandler(FlightPriceService priceService, MessageSource messageSource) {
        this.priceService = priceService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(String command) {
        return "/price".equals(command);
    }

    @Override
    public String handle(TelegramCommandContext context) {
        String[] tokens = context.tokens();

        // Expected format:
        // One-way: /price MAD AMS 2026-08-10 (4 tokens)
        // Round-trip: /price MAD AMS 2026-08-10 2026-08-17 (5 tokens)
        if (tokens.length < 4 || tokens.length > 5) {
            return messageSource.getMessage("telegram.price.invalid-format", null, context.locale());
        }

        String origin = tokens[1].toUpperCase();
        String destination = tokens[2].toUpperCase();
        LocalDate departureDate;
        LocalDate returnDate = null;

        try {
            departureDate = LocalDate.parse(tokens[3]);
            if (tokens.length == 5) {
                returnDate = LocalDate.parse(tokens[4]);
            }
        } catch (DateTimeParseException e) {
            return messageSource.getMessage("telegram.price.invalid-date", null, context.locale());
        }

        try {
            FlightPriceRequest request = new FlightPriceRequest(origin, destination, departureDate, returnDate);
            FlightPriceResponse response = priceService.getPrice(request);

            return formatResponse(response, context);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("return date")) {
                return messageSource.getMessage("telegram.error.invalid-date-range", null, context.locale());
            }
            return messageSource.getMessage("telegram.error.unexpected", null, context.locale());
        } catch (Exception e) {
            return messageSource.getMessage("telegram.error.unexpected", null, context.locale());
        }
    }

    private String formatResponse(FlightPriceResponse response, TelegramCommandContext context) {
        String returnDateStr = "";
        if (response.returnDate() != null) {
            returnDateStr = messageSource.getMessage("telegram.price.result.return-date",
                    new Object[]{response.returnDate().toString()}, context.locale()) + "\n";
        }

        return messageSource.getMessage("telegram.price.result",
                new Object[]{
                        response.origin(),
                        response.destination(),
                        response.departureDate().toString(),
                        returnDateStr,
                        response.price(),
                        response.currency()
                }, context.locale());
    }
}
