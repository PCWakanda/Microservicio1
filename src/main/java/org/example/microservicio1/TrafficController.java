package org.example.microservicio1;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Random;

@RestController
public class TrafficController {

    private final Sinks.Many<Vehicle> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Random random = new Random();
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TrafficController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        startTrafficFlow();
    }

    @GetMapping("/carretera1")
    public Flux<Vehicle> getTrafficFlow() {
        return sink.asFlux();
    }

    private void startTrafficFlow() {
        Flux.interval(Duration.ofSeconds(2))
                .subscribe(tick -> {
                    int vehicleCount = random.nextInt(3) + 1;
                    for (int i = 0; i < vehicleCount; i++) {
                        Vehicle vehicle = new Vehicle();
                        sink.tryEmitNext(vehicle);
                        rabbitTemplate.convertAndSend("logQueue", "Vehicle added: " + vehicle.getId());
                        startVehicleLifecycle(vehicle);
                    }
                });
    }

    private void startVehicleLifecycle(Vehicle vehicle) {
        Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .doOnNext(tick -> {
                    vehicle.incrementCounter();
                    rabbitTemplate.convertAndSend("logQueue", "Vehicle " + vehicle.getId() + " tick: " + vehicle.getCounter());
                })
                .doOnComplete(() -> {
                    sink.tryEmitNext(vehicle);
                    rabbitTemplate.convertAndSend("logQueue", "Vehicle removed: " + vehicle.getId());
                })
                .subscribe();
    }
}