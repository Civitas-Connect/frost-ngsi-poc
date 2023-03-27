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
package de.frachtwerk.frost_ngsi_poc.configuration;


import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManagerFactory;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Configuration
public class ParserConfiguration {

  private static final String CONFIG_FILE_NAME = "Frost.properties";
  private static final Logger LOGGER = LoggerFactory.getLogger(ParserConfiguration.class);

  private static final String KEY_TEMP_PATH = "tempPath";

  private static PluginCoreModel pluginCoreModel;
  private static ModelRegistry modelRegistry;
  private static CoreSettings coreSettings;


  private static void init() {
    coreSettings = loadCoreSettings(CONFIG_FILE_NAME);

    PersistenceManagerFactory.init(coreSettings);
    modelRegistry = new ModelRegistry();
    modelRegistry = coreSettings.getModelRegistry();

    pluginCoreModel = new PluginCoreModel();
    pluginCoreModel.init(coreSettings);
  }

  private static CoreSettings loadCoreSettings(String configFileName) {
    Properties defaults = new Properties();
    defaults.setProperty(KEY_TEMP_PATH, System.getProperty("java.io.tmpdir"));
    Properties properties = new Properties(defaults);
    try (final InputStream resourceAsStream = ParserConfiguration.class.getClassLoader()
        .getResourceAsStream(configFileName);) {
      properties.load(resourceAsStream);
      LOGGER.info("Read {} properties from {}.", properties.size(), configFileName);
    } catch (IOException exc) {
      LOGGER.info("Could not read properties from file: {}.", exc.getMessage());
    }
    return new CoreSettings(properties);
  }

  public static PluginCoreModel getPluginCoreModel() {
    if (Objects.isNull(pluginCoreModel)) {
      init();
    }
    return pluginCoreModel;
  }

  public static ModelRegistry getModelRegistry() {
    if (Objects.isNull(modelRegistry)) {
      init();
    }
    return modelRegistry;
  }

  public static CoreSettings getCoreSettings() {
    if (Objects.isNull(coreSettings)) {
      init();
    }
    return coreSettings;
  }
}
