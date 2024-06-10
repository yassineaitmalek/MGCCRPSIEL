
package com.mgcc.rpsiel.infrastructure.services.excel.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.infrastructure.exception.config.ApiException;
import com.mgcc.rpsiel.persistence.presentation.ApiDownloadInput;
import com.mgcc.rpsiel.common.utility.Utils;

import io.vavr.control.Try;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class XLSWriteService implements IWriteExcel<WritableSheet> {

  private final WritableCellFormat times;

  private final WritableCellFormat timesBoldUnderline;

  private final CellView cellView;

  private String print(Object attribute) {
    return (attribute != null) ? attribute.toString() : "";
  }

  public void addCaption(WritableSheet sheet, AtomicInteger column, AtomicInteger row, Object attribute) {

    Try.run(() -> sheet.addCell(new Label(column.getAndIncrement(), row.get(), print(attribute), timesBoldUnderline)));
    sheet.setColumnView(column.get(), cellView);
  }

  public void addLabel(WritableSheet sheet, AtomicInteger column, AtomicInteger row, Object attribute) {

    Try.run(() -> sheet.addCell(new Label(column.getAndIncrement(), row.get(), print(attribute), times)));

  }

  public <T> WritableSheet createLines(WritableSheet sheet, List<T> lines,
      QuadConsumer<T, WritableSheet, AtomicInteger, AtomicInteger> consumer) {

    AtomicInteger row = new AtomicInteger(1);
    lines
        .stream()
        .forEach(line -> {
          consumer.accept(line, sheet, row, new AtomicInteger(0));
          row.getAndIncrement();
        });
    return sheet;

  }

  public WritableSheet createHeader(WritableSheet sheet, List<String> header) {

    AtomicInteger col = new AtomicInteger(0);
    AtomicInteger row = new AtomicInteger(0);
    Utils.checkStream(header).forEach(e -> addCaption(sheet, col, row, e));
    return sheet;

  }

  public <T> byte[] exportWorkBook(String sheetName, List<String> header, List<T> lines,
      QuadConsumer<T, WritableSheet, AtomicInteger, AtomicInteger> consumer) {

    return Try.of(() -> exportWorkBookImpl(sheetName, header, lines, consumer))
        .onFailure(ApiException::reThrow)
        .get();

  }

  public <T> byte[] exportWorkBookImpl(String sheetName, List<String> header, List<T> lines,
      QuadConsumer<T, WritableSheet, AtomicInteger, AtomicInteger> consumer) throws IOException, WriteException {

    long start = System.currentTimeMillis();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    WorkbookSettings wbSettings = new WorkbookSettings();
    wbSettings.setLocale(new Locale("fr", "FR"));
    wbSettings.setRationalization(false);
    WritableWorkbook workbook = Workbook.createWorkbook(baos, wbSettings);
    workbook.createSheet(sheetName, 0);
    WritableSheet sheet = workbook.getSheet(0);

    createHeader(sheet, header);
    createLines(sheet, lines, consumer);

    workbook.write();
    workbook.close();

    long end = System.currentTimeMillis();

    log.info("time taken to export is {}s ", (end - start) / 1000);
    return baos.toByteArray();

  }

  public <T> ApiDownloadInput downloadWorkBook(String sheetName, String fileName, List<String> header,
      List<T> lines, QuadConsumer<T, WritableSheet, AtomicInteger, AtomicInteger> consumer) {

    return ApiDownloadInput.builder()
        .bytes(exportWorkBook(sheetName, header, lines, consumer))
        .fileName(fileName)
        .ext("xls")
        .build();

  }

}
