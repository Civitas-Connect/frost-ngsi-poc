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
package de.frachtwerk.frost_ngsi_poc.util;

import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.EntitySetJooqCurser;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DBHandler.class);

  private static ModelRegistry modelRegistry;
  private static CoreSettings coreSettings;

  PostgresPersistenceManager postgresPersistenceManager;

  public PostgresPersistenceManager getPostgresPersistenceManager() {
    if (Objects.isNull(postgresPersistenceManager)) {
      postgresPersistenceManager = (PostgresPersistenceManager) PersistenceManagerFactory.getInstance(coreSettings)
          .create();
    }
    return postgresPersistenceManager;
  }

  public Entity getLocationByThing(Entity thing) {
    postgresPersistenceManager = getPostgresPersistenceManager();

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        "/Things(" + thing.getId() + ")/Locations");

    Query query = QueryParser
        .parseQuery("", coreSettings, path)
        .validate();


    Entity locationEntity = null;

    if (postgresPersistenceManager.validatePath(path)) {

      try {
        Object object = postgresPersistenceManager.get(path, query);
        if (object != null) {
          for (Entity entity : (EntitySetJooqCurser) object) {
            if (Objects.isNull(locationEntity)) {
              locationEntity = entity;
            } else {
              LOGGER.error("Duplicate location found");
              break;
            }
          }
        }
      } catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }

    }
    return locationEntity;
  }
}
