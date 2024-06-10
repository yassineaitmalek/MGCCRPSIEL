
package com.mgcc.rpsiel.infrastructure.services.excel.reader;

import java.io.InputStream;
import java.util.List;

import com.mgcc.rpsiel.infrastructure.services.excel.components.ExcelSheetData;

public interface IReadExcel {

  public List<ExcelSheetData> readExcel(InputStream in, String ext);

}
