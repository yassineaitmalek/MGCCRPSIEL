package com.mgcc.rpsiel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mgcc.rpsiel.infrastructure.config.LoggingListener;

@SpringBootApplication
public class RPSIELApplication {

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(RPSIELApplication.class);
        application.addListeners(new LoggingListener());
        application.run(args);

    }

}
