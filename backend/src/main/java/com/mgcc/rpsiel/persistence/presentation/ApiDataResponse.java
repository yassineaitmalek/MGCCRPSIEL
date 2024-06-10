package com.mgcc.rpsiel.persistence.presentation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDataResponse<T> {

  private String status;

  private Integer httpStatus;

  @Builder.Default
  private String date = LocalDate.now().toString();

  @Builder.Default
  private String time = LocalTime.now().toString();

  @Builder.Default
  private String zone = ZoneId.systemDefault().toString();

  private T data;

}
