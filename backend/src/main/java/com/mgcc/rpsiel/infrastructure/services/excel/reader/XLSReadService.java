package com.mgcc.rpsiel.infrastructure.services.excel.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.common.utility.ExcelUtils;
import com.mgcc.rpsiel.common.utility.FileUtility;
import com.mgcc.rpsiel.common.utility.Utils;
import com.mgcc.rpsiel.infrastructure.services.excel.components.ExcelSheetData;

import io.vavr.control.Try;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class XLSReadService implements IReadExcel {

  public List<ExcelSheetData> readExcel(InputStream in, String ext) {

    return Try.of(() -> in)
        .mapTry(e -> readExcelImpl(in))
        .onFailure(ex -> FileUtility.closeInputStram(in))
        .onSuccess(e -> FileUtility.closeInputStram(in))
        .getOrElse(Collections.emptyList());

  }

  public ExcelSheetData getSheetData(Sheet sheet, AtomicInteger index) {

    int maxColumns = maxColumns(sheet);
    List<List<String>> rows = IntStream.range(1, sheet.getRows())
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

    return new ExcelSheetData(sheet.getName(), index.getAndIncrement(), header, rows);
  }

  public List<ExcelSheetData> readExcelImpl(InputStream in)
      throws IOException, BiffException {

    Workbook workbook = Workbook.getWorkbook(in);
    AtomicInteger index = new AtomicInteger(0);
    List<ExcelSheetData> allSheets = IntStream.range(0, workbook.getNumberOfSheets())
        .mapToObj(workbook::getSheet)
        .map(e -> getSheetData(e, index))
        .collect(Collectors.toList());
    workbook.close();
    in.close();

    return allSheets;

  }

  public int maxColumns(Sheet sheet) {

    return IntStream.range(0, sheet.getRows())
        .mapToObj(sheet::getRow)
        .map(e -> e.length)
        .max(Integer::compareTo)
        .orElse(0);

  }

  public List<String> readLine(Cell[] row, int maxColumn) {

    return IntStream.range(0, maxColumn)
        .mapToObj(index -> readLineImpl(row, index))
        .collect(Collectors.toList());

  }

  public String readLineImpl(Cell[] row, int index) {

    return Try.of(() -> row)
        .filter(Objects::nonNull)
        .mapTry(e -> e[index])
        .map(Cell::getContents)
        .map(ExcelUtils::checkField)
        .getOrNull();

  }

}
