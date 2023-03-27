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

import de.frachtwerk.frost_ngsi_poc.configuration.ParserConfiguration;
import de.frachtwerk.frost_ngsi_poc.model.Battery;
import de.frachtwerk.frost_ngsi_poc.model.Location;
import de.frachtwerk.frost_ngsi_poc.util.NGSIType;
import de.fraunhofer.iosb.ilt.frostserver.formatter.ResultFormatter;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.EntitySetJooqCurser;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncorrectRequestException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BatteryMapper extends AbstractMapper<Battery> {

  public static final String NGSI_TYPE = "NGSI_TYPE";

  public static final String AC_POWER_INPUT = "acPowerInput";
  public static final String AC_POWER_OUTPUT = "acPowerOutput";
  private static final Logger LOGGER = LoggerFactory.getLogger(BatteryMapper.class);
  public static final String PATH_THINGS = "/Things";


  private static PluginCoreModel pluginCoreModel = ParserConfiguration.getPluginCoreModel();
  private static ModelRegistry modelRegistry = ParserConfiguration.getModelRegistry();
  private static CoreSettings coreSettings = ParserConfiguration.getCoreSettings();

  PostgresPersistenceManager postgresPersistenceManager = getPostgresPersistenceManager();

  public BatteryMapper() {
  }

  public void handlePostBattery(Battery battery) {

    postgresPersistenceManager = getPostgresPersistenceManager();

    Entity thing = getEntityById(battery);
    Entity location;
    EntitySetImpl locations = new EntitySetImpl(pluginCoreModel.etLocation);

    if (Objects.isNull(thing)) {
      location = new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_LOCATION), new IdLong(0));
      setLocationEntity(location, battery);

      locations.add(location);

      try {
        postgresPersistenceManager.doInsert(location);
      } catch (Exception e) {
        LOGGER.error("Error on location insert {}", e.getMessage());
      }
      thing = createNewBattery(battery, locations, postgresPersistenceManager);
    } else {
      thing = setThingEntity(thing, battery);

      location = getLocationByThing(thing);

      if (Objects.isNull(location)) {
        location = new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_LOCATION),
            new IdLong(0));
        setLocationEntity(location, battery);
        locations.add(location);
        thing.setProperty(pluginCoreModel.npLocationsThing, locations);
        try {
          postgresPersistenceManager.doInsert(location);
          postgresPersistenceManager.doUpdate((PathElementEntity) thing.getPath().getMainElement(), thing);
        } catch (Exception e) {
          LOGGER.error("Error on location insert {}", e.getMessage());
        }
      } else {
        setLocationEntity(location, battery);

        locations.add(location);
        thing.setProperty(pluginCoreModel.npLocationsThing, locations);
        try {
          postgresPersistenceManager.doUpdate((PathElementEntity) location.getPath().getMainElement(), location);
          postgresPersistenceManager.doUpdate((PathElementEntity) thing.getPath().getMainElement(), thing);
        } catch (NoSuchEntityException | IncompleteEntityException e) {
          e.printStackTrace();
        }
      }
      try {
        postgresPersistenceManager.doUpdate((PathElementEntity) location.getPath().getMainElement(), location);
        postgresPersistenceManager.doUpdate((PathElementEntity) thing.getPath().getMainElement(), thing);
      } catch (NoSuchEntityException | IncompleteEntityException e) {
        e.printStackTrace();
      }

      try {
        postgresPersistenceManager.doUpdate((PathElementEntity) thing.getPath().getMainElement(), thing);
      } catch (NoSuchEntityException | IncompleteEntityException e) {
        e.printStackTrace();
      }
    }

    EntitySetImpl things = new EntitySetImpl(pluginCoreModel.etThing);
    things.add(thing);


    EntitySetImpl sensors = new EntitySetImpl(pluginCoreModel.etSensor);
    Entity sensor = createNewSensor(battery);
    sensors.add(sensor);


    EntitySetImpl observedProperties = new EntitySetImpl(pluginCoreModel.etSensor);
    Entity observedProperty = createNewObservedProperty(battery);
    observedProperties.add(observedProperty);


    Entity datastream = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_DATASTREAM), new IdLong(0)),
        battery);

    datastream.setProperty(pluginCoreModel.npThingDatasteam, thing);
    datastream.setProperty(pluginCoreModel.npSensorDatastream, sensor);
    datastream.setProperty(pluginCoreModel.npObservedPropertyDatastream, observedProperty);


    try {
      getPostgresPersistenceManager().doInsert(datastream);
    } catch (NoSuchEntityException | IncompleteEntityException e) {
      e.printStackTrace();
    }

    EntitySetImpl observations = new EntitySetImpl(pluginCoreModel.etObservation);
    observations.add(createNewObservation(battery, datastream));

    try {
      postgresPersistenceManager.commit(); //msg bus not configured/implemented
    } catch (Exception e) {
      LOGGER.error("FireMsg not implemented {}", e.getMessage());
    }
  }


  public PostgresPersistenceManager getPostgresPersistenceManager() {
    if (Objects.isNull(postgresPersistenceManager)) {
      postgresPersistenceManager = (PostgresPersistenceManager) PersistenceManagerFactory.getInstance(coreSettings)
          .create();
    }
    return postgresPersistenceManager;
  }


  private Entity createNewBattery(Battery battery, EntitySetImpl locations,
      PostgresPersistenceManager postgresPersistenceManager) {
    Entity thing = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_THING), new IdLong(0)), battery);
    thing.setProperty(pluginCoreModel.npLocationsThing, locations);
    try {
      postgresPersistenceManager.doInsert(thing);
    } catch (NoSuchEntityException | IncompleteEntityException e) {
      e.printStackTrace();
    }
    return thing;
  }

  private Entity createNewSensor(Battery battery) {

    Entity sensor = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_SENSOR), new IdLong(0)), battery)
        .setProperty(pluginCoreModel.epName, "Status")
        .setProperty(pluginCoreModel.epDescription, "Sensor Status")
        .setProperty(pluginCoreModel.epMetadata, "");
    try {
      getPostgresPersistenceManager().doInsert(sensor);
    } catch (NoSuchEntityException | IncompleteEntityException e) {
      e.printStackTrace();
    }
    return sensor;
  }

  private Entity createNewObservation(Battery battery, Entity datastream) {

    Entity featureOfInterest = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_FEATUREOFINTEREST), new IdLong(0)),
        battery)
        .setProperty(pluginCoreModel.epName, "FoI")
        .setProperty(pluginCoreModel.epDescription, "NGSI feature of interest");

    Entity observation = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_OBSERVATION), new IdLong(0)),
        battery)
        .setProperty(pluginCoreModel.epPhenomenonTime, new TimeValue(TimeInstant.now()))
        .setProperty(pluginCoreModel.epResultTime, TimeInstant.now())
        .setProperty(pluginCoreModel.epResult, battery.getStatus().get(0))
        .setProperty(pluginCoreModel.npDatastreamObservation, datastream)
        .setProperty(pluginCoreModel.npFeatureOfInterestObservation, featureOfInterest);


    try {
      getPostgresPersistenceManager().doInsert(featureOfInterest);
      getPostgresPersistenceManager().doInsert(observation);
    } catch (NoSuchEntityException | IncompleteEntityException e) {
      e.printStackTrace();
    }
    return observation;
  }


  private Entity createNewObservedProperty(Battery battery) {

    Entity observedProperty = setThingEntity(
        new DefaultEntity(modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_OBSERVEDPROPERTY), new IdLong(0)),
        battery)
        .setProperty(pluginCoreModel.epName, "ObservedProperty")
        .setProperty(pluginCoreModel.epDescription, "NGSI ObservedProperty");
    try {
      getPostgresPersistenceManager().doInsert(observedProperty);
    } catch (NoSuchEntityException | IncompleteEntityException e) {
      e.printStackTrace();
    }
    return observedProperty;
  }

  private Entity getEntityById(Battery battery) {

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        PATH_THINGS);

    Query query = QueryParser
        .parseQuery("$filter=name eq '" + battery.getId() + "'", coreSettings, path)
        .validate();


    Entity batteryEntity = null;

    if (postgresPersistenceManager.validatePath(path)) {
      try {
        Object result = postgresPersistenceManager.get(path, query);
        if (result != null) {
          for (Entity entity : (EntitySetJooqCurser) result) {
            if (Objects.isNull(batteryEntity)) {
              batteryEntity = entity;
            } else {
              LOGGER.error("Duplicate item found");
              break;
            }
          }
        }
      } catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }
    }
    return batteryEntity;
  }


  private Entity getLocationByThing(Entity thing) {

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


  private List<Entity> getDatastreamsByThing(Entity thing) {

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        "/Things(" + thing.getId() + ")/Datastreams");

    Query query = QueryParser
        .parseQuery("", coreSettings, path)
        .validate();

    return getEntities(postgresPersistenceManager, path, query);
  }


  private Entity getSensorByDatastream(Entity datastream) {

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        "/Datastreams(" + datastream.getId() + ")/Sensor");

    Query query = QueryParser
        .parseQuery("", coreSettings, path)
        .validate();


    Entity sensor = null;

    if (postgresPersistenceManager.validatePath(path)) {

      try {
        Object object = postgresPersistenceManager.get(path, query);
        if (object != null) {
          for (Entity entity : (EntitySetJooqCurser) object) {
            if (Objects.isNull(sensor)) {
              sensor = entity;
            } else {
              LOGGER.error("Duplicate sensor found");
              break;
            }
          }
        }
      } catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }

    }
    return sensor;
  }

  private List<Entity> getObservationsByDatastream(Entity datastream) {

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        "/Datastreams(" + datastream.getId() + ")/Observations");

    Query query = QueryParser
        .parseQuery("", coreSettings, path)
        .validate();

    return getEntities(postgresPersistenceManager, path, query);
  }

  private List<Entity> getEntities(PostgresPersistenceManager postgresPersistenceManager, ResourcePath path,
      Query query) {
    List<Entity> entities = new ArrayList<>();

    if (postgresPersistenceManager.validatePath(path)) {

      try {
        Object object = postgresPersistenceManager.get(path, query);
        if (object != null) {
          for (Entity ds : (EntitySetJooqCurser) object) {
            entities.add(ds);
          }
        }
      } catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }

    }
    return entities;
  }

  public List<Battery> getBatteries() throws IncorrectRequestException {

    QueryDefaults queryDefaults = coreSettings.getQueryDefaults()
        .setAlwaysOrder(false)
        .setUseAbsoluteNavigationLinks(true);

    ResourcePath path = PathParser.parsePath(modelRegistry, queryDefaults.getServiceRootUrl(), Version.V_1_1,
        PATH_THINGS);

    Query query = QueryParser
        .parseQuery("$filter=properties/" + NGSI_TYPE + " eq '" + NGSIType.BATTERY + "'", coreSettings, path)
        .validate();

    ResultFormatter formatter = coreSettings.getFormatter(Version.V_1_1, query.getFormat());
    formatter.preProcessRequest(path, query);

    List<Entity> entities = new ArrayList<>();

    if (postgresPersistenceManager.validatePath(path)) {

      try {
        Object object = postgresPersistenceManager.get(path, query);
        if (object != null) {
          for (Entity thing : (EntitySetJooqCurser) object) {
            entities.add(thing);
          }
          LOGGER.info(
              formatter.format(path, query, object, coreSettings.getQueryDefaults().useAbsoluteNavigationLinks())
                  .getFormatted());
        }
      } catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }

    }
    return entities.stream().map(this::toModel).toList();

  }


  public Battery getBatteryById(Long id) {

    EntityType et = modelRegistry.getEntityTypeForName(PluginCoreModel.NAME_ET_THING);

    Entity entity = postgresPersistenceManager.get(et, new IdLong(id));

    return toModel(entity);
  }


  @Override
  protected Battery createRepresentation() {
    return new Battery();
  }

  @Override
  protected Battery toModel(Entity entity) {
    Battery battery = super.toModel(entity);
    battery.setId(entity.getProperty(pluginCoreModel.epName));
    battery.setType(entity.getProperty(pluginCoreModel.epDescription));
    battery.setAcPowerInput(
        Double.valueOf((String) entity.getProperty(ModelRegistry.EP_PROPERTIES).get(AC_POWER_INPUT)));
    battery.setAcPowerOutput(
        Double.valueOf((String) entity.getProperty(ModelRegistry.EP_PROPERTIES).get(AC_POWER_OUTPUT)));

    Entity locationEntity = getLocationByThing(entity);
    if (Objects.nonNull(locationEntity)) {
      Object geoLoc = locationEntity.getProperty(pluginCoreModel.epLocation);
      if (geoLoc instanceof Point point) {
        Location location = new Location();
        List<Double> coords = new ArrayList<>();
        coords.add(point.getCoordinates().getLatitude());
        coords.add(point.getCoordinates().getLongitude());
        location.setType("Point");
        location.setCoordinates(coords);
        battery.setLocation(location);
      }
    }

    List<Entity> datastreams = getDatastreamsByThing(entity);
    for (Entity ds : datastreams) {
      List<Entity> observations = getObservationsByDatastream(ds);
      for (Entity observation : observations) {
        battery.setStatus(List.of(observation.getProperty(pluginCoreModel.epResult).toString()));
      }
    }


    return battery;
  }

  @Override
  protected Entity setThingEntity(Entity entity, Battery model) {
    entity.setProperty(pluginCoreModel.epName, model.getId())
        .setProperty(pluginCoreModel.epDescription, model.getType())
        .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
            .addProperty(NGSI_TYPE, NGSIType.BATTERY)
            .addProperty(AC_POWER_INPUT, model.getAcPowerInput().toString())
            .addProperty(AC_POWER_OUTPUT, model.getAcPowerOutput().toString())
            .build());
    return entity;
  }

  @Override
  protected Entity setLocationEntity(Entity entity, Battery model) {
    return entity.setProperty(pluginCoreModel.epName, model.getLocation().toString())
        .setProperty(pluginCoreModel.epDescription, "NGSI-location import")
        .setProperty(pluginCoreModel.epLocation, model.getLocation())
        .setProperty(ModelRegistry.EP_PROPERTIES, CollectionsHelper.propertiesBuilder()
            .addProperty(NGSI_TYPE, NGSIType.BATTERY_LOCATION)
            .build());
  }

  @Override
  protected Entity setSensorEntity(Entity entity, Battery model) {
    return null;
  }
}
