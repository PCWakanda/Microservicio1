package org.example.microservicio1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
@SpringBootApplication
@EnableDiscoveryClient
public class Microservicio1Application {

    public static void main(String[] args) {
        SpringApplication.run(Microservicio1Application.class, args);
    }

}
