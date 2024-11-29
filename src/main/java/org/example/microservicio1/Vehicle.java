package org.example.microservicio1;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    private static int vehicleCounter = 0;

    @Id
    private String id;
    private String name;
    private String roadName;
    private int tick;

    // Default constructor
    public Vehicle() {
    }

    public Vehicle(String roadName) {
        this.id = UUID.randomUUID().toString();
        this.name = "coche " + (++vehicleCounter) + " " + roadName;
        this.roadName = roadName;
        this.tick = 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoadName() {
        return roadName;
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