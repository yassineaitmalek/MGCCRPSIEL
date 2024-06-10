package com.mgcc.rpsiel.infrastructure.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.mgcc.rpsiel.common.utility.FileUtility;
import com.mgcc.rpsiel.infrastructure.config.properties.IOPaths;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IOFolders {

  private final IOPaths ioPaths;

  @PostConstruct
  public void createIOFolders() {
    log.info("creating IO folders");
    ioPaths.paths().forEach(this::createFolder);

  }

  public void createFolder(String spath) {
    Path path = Paths.get(spath).toAbsolutePath().normalize();
    if (!path.toFile().exists()) {
      FileUtility.createFolder(path);
      log.info("the folder with the path {} is created", path.toString());
    } else {
      log.info("the folder with the path {} is already created", path.toString());
    }

  }

}
