
package com.mgcc.rpsiel.infrastructure.services.excel.writer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mgcc.rpsiel.persistence.presentation.ApiDownloadInput;

public interface IWriteExcel<V> {

  public <T> byte[] exportWorkBook(String sheetName, List<String> header, List<T> lines,
      QuadConsumer<T, V, AtomicInteger, AtomicInteger> consumer);

  public <T> ApiDownloadInput downloadWorkBook(String sheetName, String fileName, List<String> header,
      List<T> lines, QuadConsumer<T, V, AtomicInteger, AtomicInteger> consumer);

  public void addLabel(V sheet, AtomicInteger column, AtomicInteger row, Object attribute);

  @FunctionalInterface
  public static interface QuadConsumer<T, V, U, W> {
    void accept(T t, V v, U u, W w);
  }

}
