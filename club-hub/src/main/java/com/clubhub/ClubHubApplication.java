package com.clubhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.clubhub.mapper")
public class ClubHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubHubApplication.class, args);
    }
}
