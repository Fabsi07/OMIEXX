package dev.ceosim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CeoSimApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeoSimApplication.class, args);
    }
}
