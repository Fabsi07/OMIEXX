package dev.omniexx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OmniexxApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmniexxApplication.class, args);
    }
}
