package io.github.luidmidev.storage.springframework.data.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnClass({EntityManagerFactory.class, FileStoredRepository.class})
@EnableJpaRepositories(basePackages = "io.github.luidmidev.storage.springframework.data.jpa")
@EntityScan(basePackages = "io.github.luidmidev.storage.springframework.data.jpa")
public class JpaStorageAutoConfiguration {

    @Bean
    public JpaStorage jpaStorage(FileStoredRepository repository) {
        return new JpaStorage(repository);
    }

}
