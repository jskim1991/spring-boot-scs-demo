package io.jay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SpringBootApplication
@RequiredArgsConstructor
public class MainApplication {

    private final Sinks.One<String> sink;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public Consumer<Message<String>> first() {
        return message -> {
            handleMessage("first/orders", message);
        };
    }

    @Bean
    public Consumer<Message<String>> second() {
        return message -> {
            handleMessage("second/deliveries", message);
        };
    }

    private void handleMessage(String log, Message<?> message) {
        Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
        if (acknowledgment != null) {
            System.out.println("*** " + log);
            System.out.println(message.getPayload());
            acknowledgment.acknowledge();
        }
    }

    @Bean
    public Supplier<Mono<String>> sinkProducer() {
        return () -> sink.asMono();
    }
}

@RestController
@RequestMapping("/produce")
@RequiredArgsConstructor
class ProducerController {

    private final StreamBridge streamBridge;
    private final Sinks.One<String> sink;

    @GetMapping("/orders")
    public void sendToOrders() {
        streamBridge.send("producer-out-0", UUID.randomUUID().toString());
    }

    @GetMapping("/deliveries")
    public void sendToDeliveries() {
        streamBridge.send("producer-out-1", UUID.randomUUID().toString());
    }

    @GetMapping("/sink")
    public void sendToOrdersSink() {
        sink.tryEmitValue("hello there");
    }
}

@Configuration
class SinkConfiguration {

    @Bean
    public Sinks.One<String> producerSink() {
//        return Sinks.many().multicast().onBackpressureBuffer();
        return Sinks.one();
    }
}