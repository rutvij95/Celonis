package com.celonis.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // For I/O bound tasks (database operations + Thread.sleep), we can have more threads than CPU cores
        executor.setCorePoolSize(5);        // Total CPU core is 8
        executor.setMaxPoolSize(10);        // 2x CPU cores (good for I/O bound tasks)
        executor.setQueueCapacity(100);      // Queue if all threads are busy
        executor.setThreadNamePrefix("CounterTask-");

        // Additional configurations for better performance
        executor.setKeepAliveSeconds(60);   // Keep extra threads alive for 60 seconds
        executor.setAllowCoreThreadTimeOut(false); // Keep core threads always alive
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
