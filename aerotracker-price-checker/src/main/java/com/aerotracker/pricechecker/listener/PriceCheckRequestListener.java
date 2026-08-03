package com.aerotracker.pricechecker.listener;

import com.aerotracker.pricechecker.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PriceCheckRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PriceCheckRequestListener.class);
    private final RabbitTemplate rabbitTemplate;

    // Inyectamos la herramienta que nos permite ENVIAR mensajes
    public PriceCheckRequestListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Escuchamos las peticiones de comprobación de precios
    @RabbitListener(queues = RabbitMQConfig.PRICE_CHECK_REQUESTS_QUEUE)
    public void handlePriceCheckRequest(String route) {
        log.info("🔍 Petición de comprobación recibida para la ruta: {}", route);

        // Simulemos que tardamos 2 segundos en consultar la API (como hacíamos antes con el Mock)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulemos que el mock nos ha devuelto que vale 50€
        String priceAlertMessage = "El precio para " + route + " ha bajado a 50€!";
        log.info("✅ Precio obtenido. Enviando mensaje al Notification Service...");

        // ¡Magia! Enviamos el mensaje a la cola del Notification Service
        // OJO: "price-alerts-queue" es el nombre exacto que le dimos en el otro servicio
        rabbitTemplate.convertAndSend("price-alerts-queue", priceAlertMessage);
    }
}