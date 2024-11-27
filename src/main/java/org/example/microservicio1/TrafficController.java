package org.example.microservicio1;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
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

    private final Sinks.Many<Vehicle> sink1 = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Vehicle> sink2 = Sinks.many().multicast().onBackpressureBuffer();
    private final Random random = new Random();
    private final RabbitTemplate rabbitTemplate;
    private final List<Vehicle> vehicles1 = new ArrayList<>();
    private final List<Vehicle> vehicles2 = new ArrayList<>();
    private int tickCount1 = 0;
    private int tickCount2 = 0;
    private final MeterRegistry meterRegistry;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public TrafficController(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry, VehicleRepository vehicleRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;
        this.vehicleRepository = vehicleRepository;
        meterRegistry.gauge("traffic.vehicles1.size", vehicles1, List::size);
        meterRegistry.gauge("traffic.vehicles2.size", vehicles2, List::size);
    }

    @PostConstruct
    public void init() {
        vehicleRepository.deleteAll();
    }

    @GetMapping("/carretera1")
    public Flux<Vehicle> getTrafficFlow1() {
        return sink1.asFlux();
    }

    @GetMapping("/carretera2")
    public Flux<Vehicle> getTrafficFlow2() {
        return sink2.asFlux();
    }

    private String formatLogMessage(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String timestamp = OffsetDateTime.now().format(formatter);
        String threadName = Thread.currentThread().getName();
        String loggerName = this.getClass().getName();
        return String.format("%s  INFO %5d --- [%s] %s : %s", timestamp, ProcessHandle.current().pid(), threadName, loggerName, message);
    }

    public void startTrafficFlow1() {
        Flux.interval(Duration.ofSeconds(4))
                .take(1)
                .subscribe(tick -> {
                    tickCount1++;
                    rabbitTemplate.convertAndSend("logQueue", formatLogMessage("----tic " + tickCount1 + " carretera 1----"));
                    rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Número de coches en carretera 1: " + vehicles1.size()));
                    if (vehicles1.size() >= 5) {
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Se ha cancelado la creación de coches en carretera 1"));
                    } else {
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Se pueden crear coches en carretera 1"));
                        int vehicleCount = random.nextInt(3) + 1;
                        for (int i = 0; i < vehicleCount; i++) {
                            Vehicle vehicle = new Vehicle("carretera 1");
                            vehicles1.add(vehicle);
                            vehicleRepository.save(vehicle);
                            sink1.tryEmitNext(vehicle);
                            rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle added: " + vehicle.getName()));
                        }
                    }
                    Iterator<Vehicle> iterator = vehicles1.iterator();
                    while (iterator.hasNext()) {
                        Vehicle vehicle = iterator.next();
                        vehicle.incrementTick();
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage(vehicle.getStatus()));
                        if (vehicle.shouldBeRemoved()) {
                            iterator.remove();
                            vehicleRepository.delete(vehicle);
                            rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle removed: " + vehicle.getName()));
                        }
                    }
                });
    }

    public void startTrafficFlow2() {
        Flux.interval(Duration.ofSeconds(4))
                .take(1)
                .subscribe(tick -> {
                    tickCount2++;
                    rabbitTemplate.convertAndSend("logQueue", formatLogMessage("----tic " + tickCount2 + " carretera 2----"));
                    rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Número de coches en carretera 2: " + vehicles2.size()));
                    if (vehicles2.size() >= 5) {
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Se ha cancelado la creación de coches en carretera 2"));
                    } else {
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Se pueden crear coches en carretera 2"));
                        int vehicleCount = random.nextInt(3) + 1;
                        for (int i = 0; i < vehicleCount; i++) {
                            Vehicle vehicle = new Vehicle("carretera 2");
                            vehicles2.add(vehicle);
                            vehicleRepository.save(vehicle);
                            sink2.tryEmitNext(vehicle);
                            rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle added: " + vehicle.getName()));
                        }
                    }
                    Iterator<Vehicle> iterator = vehicles2.iterator();
                    while (iterator.hasNext()) {
                        Vehicle vehicle = iterator.next();
                        vehicle.incrementTick();
                        rabbitTemplate.convertAndSend("logQueue", formatLogMessage(vehicle.getStatus()));
                        if (vehicle.shouldBeRemoved()) {
                            iterator.remove();
                            vehicleRepository.delete(vehicle);
                            rabbitTemplate.convertAndSend("logQueue", formatLogMessage("Vehicle removed: " + vehicle.getName()));
                        }
                    }
                });
    }
}