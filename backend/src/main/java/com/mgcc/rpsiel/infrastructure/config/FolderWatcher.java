package com.mgcc.rpsiel.infrastructure.config;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;

import com.mgcc.rpsiel.infrastructure.exception.config.ApiException;

import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class FolderWatcher implements Runnable {

  private final Path foldePath;

  private final WatchService watchService;

  private final SimpleAsyncTaskExecutor simpleAsyncTaskExecutor;

  protected FolderWatcher(String foldePath, SimpleAsyncTaskExecutor simpleAsyncTaskExecutor) {
    Objects.requireNonNull(foldePath);
    if (foldePath.trim().isEmpty()) {
      throw new IllegalArgumentException("can not accept an empty path");
    }
    this.foldePath = Paths.get(foldePath.trim()).toAbsolutePath().normalize();
    this.simpleAsyncTaskExecutor = simpleAsyncTaskExecutor;
    this.watchService = Try.of(() -> getWatchService(foldePath))
        .onFailure(ApiException::reThrow)
        .get();
  }

  @Async
  @PostConstruct
  public void startMonitoring() {
    log.info("start monitoring folder {}", foldePath);
    simpleAsyncTaskExecutor.execute(this);
  }

  @PreDestroy
  public void stopMonitoring() {
    log.info("stop monitoring folder {}", foldePath);
    Try.of(() -> watchService)
        .filter(Objects::nonNull)
        .onSuccess(FolderWatcher::closeWatchService)
        .onFailure(ex -> log.error("exception while closing the monitoring service", ex));

  }

  public static void createIOFolder(String folder) {
    log.info("creating IO folder");
    Optional.ofNullable(folder)
        .ifPresent(FolderWatcher::createFolder);

  }

  public static void createFolder(String spath) {
    createFolder(Paths.get(spath).toAbsolutePath().normalize());

  }

  public static void createFolder(Path path) {

    if (!path.toFile().exists()) {
      Try.of(() -> Files.createDirectories(path))
          .onSuccess(e -> log.info("Folder {} created successfully", e.toAbsolutePath().normalize().toString()))
          .onFailure(e -> log.error("folder {} failed to be created", path.toAbsolutePath().normalize().toString()))
          .isSuccess();
      log.info("the folder with the path {} is created", path.toString());
    } else {
      log.info("the folder with the path {} is already created", path.toString());
    }

  }

  public static WatchService getWatchService(Path myDir) throws IOException {

    createFolder(myDir);
    boolean isFolder = Optional.of(Files.getAttribute(myDir, "basic:isDirectory", NOFOLLOW_LINKS))
        .filter(Boolean.class::isInstance)
        .map(Boolean.class::cast)
        .filter(Boolean.TRUE::equals)
        .orElseThrow(() -> new RuntimeException("Path: " + myDir + " is not a folder"));

    if (isFolder) {
      log.info("Watching path: " + myDir);
    }

    return Try.of(() -> myDir)
        .map(Path::getFileSystem)
        .mapTry(FileSystem::newWatchService)
        .mapTry(e -> registerToFolder(myDir, e))
        .onFailure(ApiException::reThrow)
        .get();

  }

  public static WatchService getWatchService(String inputFolder) throws IOException {

    return getWatchService(Paths.get(inputFolder).toAbsolutePath().normalize());

  }

  private static void closeWatchService(WatchService watchService) {
    Optional.ofNullable(watchService).ifPresent(IOUtils::closeQuietly);
  }

  private static WatchService registerToFolder(Path myDir, WatchService watchService) throws IOException {
    Kind<?>[] supportedEvents = {
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY
    };
    myDir.register(watchService, supportedEvents);
    return watchService;
  }

  @Override
  public void run() {

    Try.run(this::watch)
        .onFailure(ex -> {
          Thread.currentThread().interrupt();
          ApiException.reThrow(ex);
        });

  }

  public void onFailure(Throwable ex) {
    Thread.currentThread().interrupt();
    ApiException.reThrow(ex);
  }

  public void watch() {

    WatchKey watchKey;
    do {
      watchKey = Try.of(watchService::take).onFailure(this::onFailure).get();
      watchKey.pollEvents()
          .stream()
          .filter(Objects::nonNull)
          .filter(e -> Objects.nonNull(e.context()))
          .filter(e -> e.context() instanceof Path)
          .forEach(e -> Try.run(() -> executeEvent(e.kind(),
              Paths.get(foldePath.toString(), Path.class.cast(e.context()).toString()).toAbsolutePath().normalize())));

    } while (watchKey.reset());

  }

  public void executeEvent(Kind<?> kind, Path eventPath) {
    // StandardWatchEventKinds
    switch (kind.name()) {
      case "ENTRY_CREATE":
        onCreate(eventPath);
        break;
      case "ENTRY_DELETE":
        onDelete(eventPath);
        break;
      case "ENTRY_MODIFY":
        onModify(eventPath);
        break;
      default:
        break;
    }

  }

  public abstract void onCreate(Path eventPath);

  public abstract void onDelete(Path eventPath);

  public abstract void onModify(Path eventPath);

}
