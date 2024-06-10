
package com.mgcc.rpsiel.infrastructure.services.excel;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mgcc.rpsiel.infrastructure.services.excel.components.ExcelSheetData;
import com.mgcc.rpsiel.infrastructure.services.excel.reader.IReadExcel;
import com.mgcc.rpsiel.infrastructure.services.excel.reader.XLSReadService;
import com.mgcc.rpsiel.infrastructure.services.excel.reader.XLSXReadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelService implements IReadExcel {

  private final XLSReadService xlsReadService;

  private final XLSXReadService xlsxReadService;

  @Override
  public List<ExcelSheetData> readExcel(InputStream in, String ext) {

    switch (ext.toLowerCase()) {
      case "xls":
        return xlsReadService.readExcel(in, ext);
      case "xlsx":
        return xlsxReadService.readExcel(in, ext);

      default:
        return Collections.emptyList();

    }

  }

}
