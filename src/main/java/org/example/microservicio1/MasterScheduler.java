package org.example.microservicio1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Component
public class MasterScheduler {

    private final TrafficController trafficController;
    private Disposable disposable;
    private int ticsTotales = 0;

    @Autowired
    public MasterScheduler(TrafficController trafficController) {
        this.trafficController = trafficController;
    }

    public void startSequentialFlows() {
        disposable = Flux.interval(Duration.ofSeconds(4))
                .doOnNext(tic -> {
                    ticsTotales++;
                    if (ticsTotales % 2 == 1) {
                        trafficController.startTrafficFlow1();
                    } else {
                        trafficController.startTrafficFlow2();
                    }
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }
}