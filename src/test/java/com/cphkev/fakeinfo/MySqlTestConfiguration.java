package com.cphkev.fakeinfo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestConfiguration {

    @Bean
    @ServiceConnection
    public MySQLContainer mySQLContainer() {
        return new MySQLContainer("mysql:9.5.0")
                .withDatabaseName("addresses")
                .withUsername("test")
                .withPassword("test");
    }
}
