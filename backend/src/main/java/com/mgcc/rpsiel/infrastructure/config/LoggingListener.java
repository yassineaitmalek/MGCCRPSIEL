package com.mgcc.rpsiel.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.lang.NonNull;

@Configuration
public class LoggingListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  @Override
  public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {

    Optional.ofNullable(event)
        .map(ApplicationEnvironmentPreparedEvent::getEnvironment)
        .ifPresent(this::addPropertiesToSystem);

  }

  public void addPropertiesToSystem(ConfigurableEnvironment environment) {

    Stream.of(environment.getPropertySources().spliterator())
        .filter(EnumerablePropertySource.class::isInstance)
        .map(EnumerablePropertySource.class::cast)
        .map(EnumerablePropertySource::getPropertyNames)
        .map(Arrays::asList)
        .flatMap(List::stream)
        .forEach(propName -> addPropertToSystem(propName, environment));

  }

  public void addPropertToSystem(String propName, ConfigurableEnvironment environment) {

    Optional.ofNullable(propName)
        .map(environment::getProperty)
        .ifPresent(value -> System.setProperty(propName, value));
  }

}
