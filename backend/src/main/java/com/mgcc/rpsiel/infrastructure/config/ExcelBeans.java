package com.mgcc.rpsiel.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jxl.CellView;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;

@Configuration
public class ExcelBeans {

  @Bean
  public WritableCellFormat times() throws WriteException {

    WritableCellFormat times = new WritableCellFormat(new WritableFont(WritableFont.TIMES, 10));
    times.setAlignment(Alignment.CENTRE);
    return times;

  }

  @Bean
  public WritableCellFormat timesBoldUnderline() throws WriteException {
    WritableCellFormat timesBoldUnderline = new WritableCellFormat(
        new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false,
            UnderlineStyle.SINGLE));
    timesBoldUnderline.setAlignment(Alignment.CENTRE);
    return timesBoldUnderline;

  }

  @Bean
  public CellView createCellView() throws WriteException {
    CellView cv = new CellView();
    cv.setFormat(timesBoldUnderline());
    cv.setAutosize(true);
    return cv;

  }

}
