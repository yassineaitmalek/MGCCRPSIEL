
package com.mgcc.rpsiel.infrastructure.services.excel.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.common.utility.CheckUtility;
import com.mgcc.rpsiel.common.utility.DateUtility;
import com.mgcc.rpsiel.common.utility.ExcelUtils;
import com.mgcc.rpsiel.common.utility.FileUtility;
import com.mgcc.rpsiel.common.utility.Utils;
import com.mgcc.rpsiel.infrastructure.services.excel.components.ExcelSheetData;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class XLSXReadService implements IReadExcel {

  public List<ExcelSheetData> readExcel(InputStream in, String ext) {

    return Try.of(() -> in)
        .mapTry(e -> readExcelImpl(in))
        .onFailure(ex -> FileUtility.closeInputStram(in))
        .onSuccess(e -> FileUtility.closeInputStram(in))
        .getOrElse(Collections.emptyList());

  }

  public List<ExcelSheetData> readExcelImpl(InputStream in)
      throws IOException {

    XSSFWorkbook workbook = new XSSFWorkbook(in);
    AtomicInteger index = new AtomicInteger(0);
    List<ExcelSheetData> allSheets = IntStream.range(0, workbook.getNumberOfSheets())
        .mapToObj(workbook::getSheetAt)
        .map(e -> getSheetData(e, index))
        .collect(Collectors.toList());

    workbook.close();
    in.close();

    return allSheets;

  }

  public int maxColumns(Sheet sheet) {

    return IntStream.range(0, sheet.getPhysicalNumberOfRows())
        .mapToObj(sheet::getRow)
        .map(Row::getPhysicalNumberOfCells)
        .max(Integer::compareTo)
        .orElseGet(() -> 0);

  }

  public ExcelSheetData getSheetData(XSSFSheet sheet, AtomicInteger index) {

    int maxColumns = maxColumns(sheet);
    List<List<String>> rows = IntStream.range(1, sheet.getPhysicalNumberOfRows())
        .mapToObj(sheet::getRow)
        .map(row -> readLine(row, maxColumns))
        .filter(Utils.not(ExcelUtils::checkEmptyStringList))
        .collect(Collectors.toList());

    List<String> header = IntStream.range(0, 1)
        .mapToObj(sheet::getRow)
        .map(row -> readLine(row, maxColumns))
        .filter(Utils.not(ExcelUtils::checkEmptyStringList))
        .findFirst()
        .orElseGet(Collections::emptyList);

    return new ExcelSheetData(sheet.getSheetName(), index.getAndIncrement(), header, rows);
  }

  public String getCellValue(Cell cell) {
    CellType celleType = cell.getCellType();
    if (celleType.equals(CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell)) {
      return String.valueOf(DateUtility.getSimpleDateFormat().format(cell.getDateCellValue()));
    } else if (celleType.equals(CellType.NUMERIC)) {
      return CheckUtility.formatDoubleXls(cell.getNumericCellValue());
    } else if (celleType.equals(CellType.STRING)) {
      return CheckUtility.checkString(cell.getStringCellValue());

    } else if (celleType.equals(CellType.BOOLEAN)) {
      return String.valueOf(cell.getBooleanCellValue());
    } else if (celleType.equals(CellType.FORMULA)) {
      return String.valueOf(cell.getCellFormula());
    } else if (celleType.equals(CellType.ERROR)) {
      return String.valueOf(cell.getErrorCellValue());
    } else {
      return null;
    }

  }

  public List<String> readLine(Row row, int maxColumn) {

    return IntStream.range(0, maxColumn)
        .mapToObj(index -> readLineImpl(row, index))
        .collect(Collectors.toList());

  }

  public String readLineImpl(Row row, int index) {

    return Try.of(() -> row)
        .filter(Objects::nonNull)
        .mapTry(e -> e.getCell(index))
        .map(this::getCellValue)
        .map(ExcelUtils::checkField)
        .getOrNull();

  }

}
