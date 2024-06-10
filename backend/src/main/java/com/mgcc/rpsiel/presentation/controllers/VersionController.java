package com.mgcc.rpsiel.presentation.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgcc.rpsiel.infrastructure.config.properties.AppVersionProperies;
import com.mgcc.rpsiel.persistence.dto.VersionDTO;
import com.mgcc.rpsiel.persistence.presentation.ApiDataResponse;
import com.mgcc.rpsiel.presentation.config.AbstractController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController implements AbstractController {

  private final AppVersionProperies appVersionProperies;

  @GetMapping
  public ResponseEntity<ApiDataResponse<VersionDTO>> getVersion() {
    return ok(() -> new VersionDTO(appVersionProperies.getVersion()));
  }

}
