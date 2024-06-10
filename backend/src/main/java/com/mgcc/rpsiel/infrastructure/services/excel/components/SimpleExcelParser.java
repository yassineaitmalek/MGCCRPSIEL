package com.mgcc.rpsiel.infrastructure.services.excel.components;

import java.util.List;
import java.util.Optional;

public interface SimpleExcelParser<T> {

  public Optional<T> parse(List<String> row);

}
