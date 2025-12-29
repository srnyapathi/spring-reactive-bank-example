package in.srnyapathi.bank.application;


import in.srnyapathi.bank.config.ApiModuleConfiguration;
import in.srnyapathi.bank.metrics.config.MetricsModuleConfiguration;
import in.srnyapathi.bank.persistence.config.PersistenceModuleConfig;
import in.srnyapathi.bank.domain.config.DomainModuleConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;


@SpringBootApplication
@ComponentScan(basePackages = {"in.srnyapathi"})
@Import({PersistenceModuleConfig.class,
        DomainModuleConfiguration.class,
        ApiModuleConfiguration.class,
        MetricsModuleConfiguration.class})
@Configuration
@EnableAutoConfiguration
@EnableR2dbcRepositories(basePackages = "in.srnyapathi.bank.persistence.repository")
@EntityScan(basePackages = "in.srnyapathi.bank.persistence.entity")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}