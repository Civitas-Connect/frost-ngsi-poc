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
package de.frachtwerk.frost_ngsi_poc.mapper;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;

public abstract class AbstractMapper<M> {

  protected abstract M createRepresentation();

  protected M toModel(Entity entity) {
    return createRepresentation();
  }


  protected abstract Entity setThingEntity(Entity entity, M model);

  protected abstract Entity setLocationEntity(Entity entity, M model);

  protected abstract Entity setSensorEntity(Entity entity, M model);
}
