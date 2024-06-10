package com.mgcc.rpsiel.persistence.models.local.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

  private String fullName;

  private String adress;

  private String cin;

  private String rib;

  private CodeBank bank;

}
