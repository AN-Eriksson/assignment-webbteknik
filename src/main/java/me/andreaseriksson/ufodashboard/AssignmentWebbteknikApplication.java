package me.andreaseriksson.ufodashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AssignmentWebbteknikApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssignmentWebbteknikApplication.class, args);
    }

}
