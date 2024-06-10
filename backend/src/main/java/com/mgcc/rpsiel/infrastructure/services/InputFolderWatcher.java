package com.mgcc.rpsiel.infrastructure.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.common.utility.FileUtility;
import com.mgcc.rpsiel.common.utility.Utils;
import com.mgcc.rpsiel.infrastructure.config.FolderWatcher;
import com.mgcc.rpsiel.infrastructure.config.properties.IOPaths;
import com.mgcc.rpsiel.infrastructure.exception.config.ApiException;
import com.mgcc.rpsiel.infrastructure.services.excel.ExcelService;
import com.mgcc.rpsiel.infrastructure.services.excel.components.ExcelSheetData;
import com.mgcc.rpsiel.infrastructure.services.excel.template.ExcelTemplate;
import com.mgcc.rpsiel.infrastructure.services.parser.PersonExcelParser;
import com.mgcc.rpsiel.infrastructure.services.word.WordTemplate;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InputFolderWatcher extends FolderWatcher {

  private final IOPaths ioPaths;

  private final ExcelService excelService;

  private final ExcelTemplate excelTemplate;

  private final WordTemplate wordTemplate;

  private final PersonExcelParser personExcelParser;

  public InputFolderWatcher(SimpleAsyncTaskExecutor simpleAsyncTaskExecutor,
      ExcelService excelService,
      IOPaths ioPaths,
      ExcelTemplate excelTemplate,
      PersonExcelParser personExcelParser, WordTemplate wordTemplate) {
    super(ioPaths.getInput(), simpleAsyncTaskExecutor);
    this.excelService = excelService;
    this.ioPaths = ioPaths;
    this.excelTemplate = excelTemplate;
    this.personExcelParser = personExcelParser;
    this.wordTemplate = wordTemplate;

  }

  public List<ExcelSheetData> getData(File file) {

    InputStream in = Try.of(() -> file).mapTry(FileInputStream::new).onFailure(ApiException::reThrow).get();
    return excelService.readExcel(in, FileUtility.getFileExtension(file));
  }

  public void main(Path filePath) {

    Optional.ofNullable(filePath)
        .map(Path::toFile)
        .map(this::getData)
        .filter(Utils.not(List::isEmpty))
        .map(e -> e.get(0))
        .map(e -> e.parse(personExcelParser))
        .orElseGet(Collections::emptyList)
        .stream()
        .forEach(e -> {
          excelTemplate.createEtat(e);
          wordTemplate.createOV(e);
        });

    moveToAccepted(filePath);

  }

  public void moveToAccepted(Path filePath) {
    Path to = Paths
        .get(Paths.get(ioPaths.getAccepted()).toAbsolutePath().normalize().toString(), filePath.toFile().getName())
        .toAbsolutePath().normalize();
    FileUtility.moveFile(filePath.toString(), to.toString());
  }

  public void moveToError(Path filePath) {
    Path to = Paths
        .get(Paths.get(ioPaths.getError()).toAbsolutePath().normalize().toString(), filePath.toFile().getName())
        .toAbsolutePath().normalize();
    FileUtility.moveFile(filePath.toString(), to.toString());
  }

  public void fail(Throwable throwable, Path filePath) {

    moveToError(filePath);
    log.error(throwable.getMessage(), throwable);
  }

  public void onCreate(Path eventPath) {
    log.info("something is created in {}", eventPath);
    CompletableFuture.runAsync(() -> exectue(eventPath));
  }

  public void exectue(Path eventPath) {
    Utils.sleepSeconds(3);
    Try.run(() -> main(eventPath)).onFailure(ex -> fail(ex, eventPath));

  }

  public void onDelete(Path eventPath) {
    log.info("something is deleted in {}", eventPath);
  }

  public void onModify(Path eventPath) {
    log.info("something is modifiyed in {}", eventPath);
  }

}
