/*
 * Copyright (C) 2023 Civitas Connect e.V., Hafenweg 7, D 48155 Münster.
 *
 * This file is part of frost-ngsi-poc.
 *
 * frost-ngsi-poc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * frost-ngsi-poc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with frost-ngsi-poc. If not, see <http://www.gnu.org/licenses/>.
 */
package de.frachtwerk.frost_ngsi_poc.controller;


import java.util.*;

import de.frachtwerk.frost_ngsi_poc.mapper.BatteryMapper;
import de.frachtwerk.frost_ngsi_poc.model.Battery;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/battery")
public class BatteryController {

  private final BatteryMapper batteryMapper;

  @Autowired
  public BatteryController(BatteryMapper batteryMapper) {
    this.batteryMapper = batteryMapper;
  }

  @GetMapping("")
  public List<Battery> findAll() throws IncorrectRequestException {
    return batteryMapper.getBatteries();
  }

  @GetMapping("/{id}")
  public Battery findById(@PathVariable("id") final Long id) {
    return batteryMapper.getBatteryById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Battery create(@RequestBody Battery battery) {
    batteryMapper.handlePostBattery(battery);
    return battery;
  }

  @PutMapping(value = "/{id}")
  public Battery updateObject() {
    throw new NotImplementedException();
  }

  @PatchMapping(value = "/{id}")
  public Battery update() {
    throw new NotImplementedException();
  }

  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") final Long id) {
    throw new NotImplementedException();
  }
}
