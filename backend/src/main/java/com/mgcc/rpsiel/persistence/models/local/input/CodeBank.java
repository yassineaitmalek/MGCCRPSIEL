package com.mgcc.rpsiel.persistence.models.local.input;

import java.util.Optional;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeBank {
  C021("021", "CRÉDIT DU MAROC "),
  C013("013", "BMCI "),
  C118("118", ""),
  C112("112", ""),
  C214("214", ""),
  C230("230", "CIH BANK"),
  C225("225", "CREDIT AGRICOLE"),
  C050("050", "CFG BANK"),
  C007("007", "ATTIJARIWAFA BANK"),
  C011("011", "BMCE"),
  C190("190", "BANQUE POPULAIRE"),
  C022("022", "SOCIÉTÉ GÉNÉRALE"),
  C181("181", "BANQUE POPULAIRE"),
  C360("360", ""),
  C350("350", "AL BARID BANK"),
  C310("310", "BANK AL MAGHRIB"),
  C150("150", "BANQUE POPULAIRE"),
  C145("145", "BANQUE POPULAIRE"),
  C127("127", "BANQUE POPULAIRE"),
  C157("157", "BANQUE POPULAIRE"),
  C164("164", "BANQUE POPULAIRE"),
  C101("101", "BANQUE POPULAIRE"),
  C30002("30002", "LCL - Le Crédit Lyonnais"),
  C143("143", "BANQUE POPULAIRE"),

  CNAN("", "");

  private final String value;

  private final String label;

  public static CodeBank of(String value) {
    return Stream.of(values())
        .filter(e -> e.getValue().equals(value.trim()))
        .findFirst()
        .orElseGet(() -> CNAN);
  }

}
