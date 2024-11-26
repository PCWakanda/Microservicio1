package org.example.microservicio1;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@RestController
public class TrafficController {

    private final Sinks.Many<Vehicle> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Random random = new Random();
    private final RabbitTemplate rabbitTemplate;
    private final List<Vehicle> vehicles = new ArrayList<>();

    @Autowired
    public TrafficController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/carretera1")
    public Flux<Vehicle> getTrafficFlow() {
        return sink.asFlux();
    }

    private String formatLogMessage(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String timestamp = OffsetDateTime.now().format(formatter);
        String threadName = Thread.currentThread().getName();
        String loggerName = this.getClass().getName();
        return String.format("%s  INFO %5d --- [%s] %s : %s", timestamp, ProcessHandle.current().pid(), threadName, loggerName, message);
    }

    public void startTrafficFlow() {
        Flux.interval(Duration.ofSeconds(4))
                .subscribe(tick -> {
                    rabbitTemplate.convertAndSend("logQueue", formatLogMessage("----tic " + (tick + 1) + "----"));
                    int vehicleCount = random.nextInt(3) + 1;
                    for (int i = 0; i < vehicleCount; i++) {
                        Vehicle vehicle = new Vehicle();
                        vehicles.add(vehicle);
                        sink.tryEmitNext(vehicle);
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle added: " + vehicle.getName()));
                    }
                    Iterator<Vehicle> iterator = vehicles.iterator();
                    while (iterator.hasNext()) {
                        Vehicle vehicle = iterator.next();
                        vehicle.incrementTick();
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage(vehicle.getStatus()));
                        if (vehicle.shouldBeRemoved()) {
                            iterator.remove();
                            rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle removed: " + vehicle.getName()));
                        }
                    }
                });
    }
}