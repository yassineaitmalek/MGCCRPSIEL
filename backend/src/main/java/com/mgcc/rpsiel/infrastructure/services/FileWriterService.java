package com.mgcc.rpsiel.infrastructure.services;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.infrastructure.exception.config.ServerSideException;
import com.mgcc.rpsiel.persistence.presentation.ApiDownloadInput;

import io.vavr.control.Try;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileWriterService {

  public String saveToTXTFile(StringBuilder stringBuilder, String folderPath, String fileName) {
    String fullPath = Paths.get(folderPath, fileName + "." + "txt").toAbsolutePath().normalize().toString();
    return Try.of(() -> saveToTXTFileThrow(stringBuilder, fullPath, fileName))
        .onFailure(ServerSideException::reThrow)
        .get();

  }

  public String saveToTXTFileThrow(StringBuilder stringBuilder, String fullPath, String fileName) throws IOException {

    @Cleanup
    FileWriter fileWriter = new FileWriter(fullPath);

    @Cleanup
    BufferedWriter writer = new BufferedWriter(fileWriter);

    log.info("file {} is created", fileName);
    log.info("start writing to file {}", fileName);

    writer.append(Optional.ofNullable(stringBuilder).map(StringBuilder::toString).orElseGet(String::new));
    writer.close();
    log.info("writing to file {} is complete", fileName);
    log.info("the full path of the file  {}", fullPath);
    return fullPath;

  }

  public ApiDownloadInput generateTXTBytes(StringBuilder stringBuilder, String fileName) {

    return Try.of(() -> generateTXTBytesThrow(stringBuilder))
        .map(ByteArrayOutputStream::toByteArray)
        .map(e -> ApiDownloadInput.builder().bytes(e).fileName(fileName).ext("txt").build())
        .onFailure(ServerSideException::reThrow)
        .get();

  }

  public ByteArrayOutputStream generateTXTBytesThrow(StringBuilder stringBuilder) throws IOException {

    @Cleanup
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    log.info("start writing to OutputStream ");

    baos.write(Optional.ofNullable(stringBuilder).map(StringBuilder::toString).orElseGet(String::new)
        .getBytes(StandardCharsets.UTF_8));
    baos.close();
    log.info("writing to OutputStream is complete");
    return baos;

  }

}
