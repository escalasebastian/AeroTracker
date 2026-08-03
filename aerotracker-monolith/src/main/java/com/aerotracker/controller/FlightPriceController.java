package com.aerotracker.controller;

import com.aerotracker.dto.FlightPriceRequest;
import com.aerotracker.dto.FlightPriceResponse;
import com.aerotracker.service.FlightPriceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flights")
public class FlightPriceController {
    private final FlightPriceService priceService;

    public FlightPriceController(FlightPriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping("/price")
    public FlightPriceResponse getPrice(@RequestBody FlightPriceRequest request) {
        return priceService.getPrice(request);
    }
}
