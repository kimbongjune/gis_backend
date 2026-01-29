package kr.co.saeron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GisApplication {

    public static void main(String[] args) {
        SpringApplication.run(GisApplication.class, args);
    }
    @org.springframework.context.annotation.Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder();
    }
}
