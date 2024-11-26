package org.example.microservicio1;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Vehicle {
    private final String id;
    private final AtomicInteger counter;

    public Vehicle() {
        this.id = UUID.randomUUID().toString();
        this.counter = new AtomicInteger(0);
    }

    public String getId() {
        return id;
    }

    public int getCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.incrementAndGet();
    }
}