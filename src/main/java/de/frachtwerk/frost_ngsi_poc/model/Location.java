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
package de.frachtwerk.frost_ngsi_poc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "coordinates"})
@Data
public class Location {

  @JsonProperty("type")
  private String type;

  @JsonProperty("coordinates")
  private List<Double> coordinates = null;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}
