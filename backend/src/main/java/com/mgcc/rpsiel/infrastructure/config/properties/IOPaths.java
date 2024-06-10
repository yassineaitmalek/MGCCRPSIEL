package com.mgcc.rpsiel.infrastructure.config.properties;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "mgcc.rpsiel.io")
public class IOPaths {

  private String output;

  private String input;

  private String accepted;

  private String error;

  public List<String> paths() {
    return Arrays.asList(output, input, accepted, error);
  }

}
