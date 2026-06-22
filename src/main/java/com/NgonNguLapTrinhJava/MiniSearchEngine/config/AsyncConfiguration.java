package com.NgonNguLapTrinhJava.MiniSearchEngine.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor(
            @Value("${mini-search.mail.async.core-pool-size}") int corePoolSize,
            @Value("${mini-search.mail.async.max-pool-size}") int maxPoolSize,
            @Value("${mini-search.mail.async.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("mail-smtp-");
        executor.initialize();
        return executor;
    }
}
