package org.example.microservicio1;

import java.util.UUID;

public class Vehicle {
    private static int vehicleCounter = 0;
    private String id;
    private String name;
    private int tick;
    private String roadName;

    public Vehicle(String roadName) {
        this.id = UUID.randomUUID().toString();
        this.name = "coche " + (++vehicleCounter) + " " + roadName;
        this.tick = 0;
        this.roadName = roadName;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTick() {
        return tick;
    }

    public void incrementTick() {
        this.tick++;
    }

    public String getStatus() {
        return name + " lleva " + tick + " tics con vida";
    }

    public boolean shouldBeRemoved() {
        return tick >= 5;
    }
}