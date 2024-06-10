package com.mgcc.rpsiel.infrastructure.services.parser;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mgcc.rpsiel.infrastructure.services.excel.components.SimpleExcelParser;
import com.mgcc.rpsiel.persistence.models.local.input.CodeBank;
import com.mgcc.rpsiel.persistence.models.local.input.Person;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersonExcelParser implements SimpleExcelParser<Person> {

  @Override
  public Optional<Person> parse(List<String> row) {
    if (row.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(Person.builder()
        .fullName(Optional.ofNullable(row.get(0)).orElseGet(String::new))
        .adress(Optional.ofNullable(row.get(1)).orElseGet(String::new))
        .cin(Optional.ofNullable(row.get(2)).orElseGet(String::new))
        .rib(Optional.ofNullable(row.get(3)).map(e -> e.replace("'", "")).orElseGet(String::new))
        .bank(CodeBank.of(Optional.ofNullable(row.get(3)).map(e -> e.replace("'", ""))
            .map(e -> e.substring(0, 3)).orElseGet(String::new)))
        .build());
  }

}
