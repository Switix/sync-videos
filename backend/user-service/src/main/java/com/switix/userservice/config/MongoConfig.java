package com.switix.userservice.config;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@AllArgsConstructor
public class MongoConfig {

    private MongoTemplate mongoTemplate;

    @PreDestroy
    public void dropCollections() {
        // Drop collections
        mongoTemplate.getDb().drop();
    }
}
