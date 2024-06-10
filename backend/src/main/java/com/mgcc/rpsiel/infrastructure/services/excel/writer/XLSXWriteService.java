
package com.mgcc.rpsiel.infrastructure.services.excel.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FontFamily;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.infrastructure.exception.config.ApiException;
import com.mgcc.rpsiel.persistence.presentation.ApiDownloadInput;
import com.mgcc.rpsiel.common.utility.Utils;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class XLSXWriteService implements IWriteExcel<XSSFSheet> {

  public XSSFFont font(XSSFWorkbook workbook) {

    XSSFFont font = workbook.createFont();
    font.setColor(IndexedColors.BLACK.index);
    font.setBold(true);
    font.setFontHeight(10);
    font.setFontHeightInPoints((short) (10));
    font.setFamily(FontFamily.MODERN);
    return font;
  }

  public CellStyle style(XSSFWorkbook workbook) {

    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setFont(font(workbook));
    return style;

  }

  public void autoSize(XSSFSheet sheet) {

    IntStream.range(0, sheet.getRow(0).getPhysicalNumberOfCells())
        .forEach(sheet::autoSizeColumn);

  }

  private String print(Object attribute) {
    return (attribute != null) ? attribute.toString() : "";
  }

  public Row getOrCreateRow(XSSFSheet sheet, AtomicInteger rowIndex) {

    Row row = sheet.getRow(rowIndex.get());
    if (row == null) {

      row = sheet.createRow(rowIndex.get());
    }
    return row;

  }

  public void addCaption(XSSFSheet sheet, AtomicInteger column, AtomicInteger row, Object attribute,
      CellStyle style) {

    Cell cell = getOrCreateRow(sheet, row).createCell(column.get());
    cell.setCellValue(print(attribute));
    cell.setCellStyle(style);
    column.getAndIncrement();

  }

  public void addLabel(XSSFSheet sheet, AtomicInteger column, AtomicInteger row, Object attribute) {

    getOrCreateRow(sheet, row).createCell(column.get()).setCellValue(print(attribute));
    column.getAndIncrement();

  }

  public XSSFSheet createHeader(XSSFSheet sheet, List<String> header, CellStyle style) {

    AtomicInteger col = new AtomicInteger(0);
    AtomicInteger row = new AtomicInteger(0);
    Utils.checkStream(header).forEach(e -> addCaption(sheet, col, row, e, style));
    return sheet;

  }

  public <T> XSSFSheet createLines(XSSFSheet sheet, List<T> lines,
      QuadConsumer<T, XSSFSheet, AtomicInteger, AtomicInteger> consumer) {

    AtomicInteger row = new AtomicInteger(1);
    Utils.checkStream(lines)
        .forEach(line -> {
          consumer.accept(line, sheet, row, new AtomicInteger(0));
          row.getAndIncrement();
        });
    return sheet;

  }

  public <T> byte[] exportWorkBook(String sheetName, List<String> header, List<T> lines,
      QuadConsumer<T, XSSFSheet, AtomicInteger, AtomicInteger> consumer) {

    return Try.of(() -> exportWorkBookImpl(sheetName, header, lines, consumer))
        .onFailure(ApiException::reThrow)
        .get();

  }

  public <T> byte[] exportWorkBookImpl(String sheetName, List<String> header, List<T> lines,
      QuadConsumer<T, XSSFSheet, AtomicInteger, AtomicInteger> consumer) throws IOException {

    long start = System.currentTimeMillis();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet(sheetName);
    CellStyle style = style(workbook);
    createHeader(sheet, header, style);
    createLines(sheet, lines, consumer);
    // to auto fit data autoSize(sheet)
    workbook.write(baos);
    workbook.close();

    long end = System.currentTimeMillis();

    log.info("time taken to export is {}s ", (end - start) / 1000);
    return baos.toByteArray();

  }

  public <T> ApiDownloadInput downloadWorkBook(String sheetName, String fileName, List<String> header,
      List<T> lines, QuadConsumer<T, XSSFSheet, AtomicInteger, AtomicInteger> consumer) {

    return ApiDownloadInput.builder()
        .bytes(exportWorkBook(sheetName, header, lines, consumer))
        .fileName(fileName)
        .ext("xlsx")
        .build();

  }

}
