package pl.edu.ur.blokur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punkt wejścia aplikacji Blokur.
 */
@SpringBootApplication
@EnableAsync
public class BlokurApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlokurApplication.class, args);
    }
}
