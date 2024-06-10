package com.mgcc.rpsiel.infrastructure.config;

import java.util.concurrent.Semaphore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setQueueCapacity(100);
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(8);
    executor.setThreadNamePrefix("mgccThread-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "semaphore")
  public Semaphore semaphore() {

    return new Semaphore(1);
  }

  @Bean

  public SimpleAsyncTaskExecutor getSimpleAsyncTaskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

}