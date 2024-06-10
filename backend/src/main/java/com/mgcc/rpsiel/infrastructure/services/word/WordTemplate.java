package com.mgcc.rpsiel.infrastructure.services.word;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.common.utility.FileUtility;
import com.mgcc.rpsiel.infrastructure.config.properties.IOPaths;
import com.mgcc.rpsiel.infrastructure.exception.config.ApiException;
import com.mgcc.rpsiel.persistence.models.local.input.Person;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordTemplate {

  private final IOPaths ioPaths;

  public InputStream loadResource(String path) {
    return Try.of(() -> path)
        .map(ClassPathResource::new)
        .mapTry(ClassPathResource::getInputStream)
        .getOrElseThrow(() -> new ApiException("error loading the resource " + path));

  }

  public void applyRun(XWPFRun run, Person person) {
    String text = run.getText(0);
    if (Objects.isNull(text)) {
      return;
    }
    if (Objects.nonNull(person.getFullName())) {
      text = text.replace("${name}", person.getFullName());
    }
    if (Objects.nonNull(person.getBank())) {
      text = text.replace("${bank}", person.getBank().getLabel());
    }
    if (Objects.nonNull(person.getRib())) {
      text = text.replace("${rib}", person.getRib());
    }
    run.setText(text, 0);

  }

  public void createOV(Person person) {
    FileUtility
        .createFolder(Paths.get(ioPaths.getOutput(), person.getFullName()));
    try (XWPFDocument document = new XWPFDocument(loadResource("templates/O.V.docx"));
        FileOutputStream fos = new FileOutputStream(
            Paths.get(ioPaths.getOutput(), person.getFullName(), "OV_" + person.getFullName() + ".docx").toString())) {

      document.getParagraphs()
          .stream()
          .map(XWPFParagraph::getRuns)
          .flatMap(e -> e.stream())
          .forEach(e -> applyRun(e, person));

      document.write(fos);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
