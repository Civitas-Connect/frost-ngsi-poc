# FROST-NGSI-POC

In this proof of concept we aim to convert a [NGSI-LD](https://en.wikipedia.org/wiki/NGSI-LD) data structure into the
[SensorThings API](https://en.wikipedia.org/wiki/SensorThings_API) data structure and back while storing the data only
once. With that we want to show that the STA and NGSI standards can be used interoperable without redundant data
storing.

## Getting started

Our PoC relies on the [FROST-Server](https://github.com/FraunhoferIOSB/FROST-Server) as an implementation of the
SensorThings API (STA).
FROST provides a docker configuration as
a [docker-compose](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/v2.x/scripts/docker-compose.yaml) file
which can be used with one adjustment.
Furthermore, we use the FROST STA data model and the database handler.

The PoC is a rudimentary SpringBoot application and is configured through the application.yml.

### Run the application

#### Prerequisites

To run the application you need to install docker with the docker compose plugin and a JDK version 17 or above as well
as maven.

1. Download the docker-compose file mentioned above
2. Add the following lines for the database service

```yaml
    ports:
      - 5432:5432
```

3. Start the FROST-Server with `docker compose up`
4. In your browser navigate to `http://localhost:8080/FROST-Server`, click on `Database Status and Update` and
   click `Do Update`
5. Execute `mvn spring-boot:run` in the repositories root directory to start the application

### API

We focus on storing and retrieving information to and from FROST.
Our API provides 2 Endpoints in order to read data from the FROST database and to store data into it.

#### GET v1/battery/{id}

Making a request with a valid FROST id will return the data, aggregating each entity from STA into one NGSI entity.

The call `{server}/battery/1` will return a shortened version of the NGSI data:

```json
{
  "id": "urn:ngsi-ld:Battery:santander:d95372df391",
  "type": "Battery",
  "acPowerInput": 1.55,
  "acPowerOutput": 2.5,
  "location": {
    "type": "Point",
    "coordinates": [
      41.640833333,
      -4.75421
    ]
  },
  "status": [
    "working"
  ]
}
```

#### POST v1/battery

This allows us to store NGSI data into our FROST database.
At the moment only the complete data model from NGSI can be stored.
The data can be stored idempotent, like seen with the thing entity.
Other entities are currently not idempotent.

The POST to `{server}/battery` with a request body like:

```json
{
  "id": "urn:ngsi-ld:Battery:santander:d95372df391",
  "type": "Battery",
  "acPowerInput": 1.55,
  "acPowerOutput": 2.5,
  "autonomyTime": "PT1H",
  "cycleLife": 20000,
  "dataProvider": "bike-in.com",
  "location": {
    "type": "Point",
    "coordinates": [
      -4.75421,
      41.640833333
    ]
  },
  "rechargeTime": "PT6H",
  "refDevice": "urn:ngsi-ld:Device:santander:d95372df39",
  "source": "bike-in.com",
  "status": [
    "working"
  ],
  "@context": [
    "https://smart-data-models.github.io/data-models/context.jsonld",
    "https://raw.githubusercontent.com/smart-data-models/dataModel.Battery/master/context.jsonld"
  ]
}
```

will generate all necessary FROST entities and stores them within the database.

## Current state of development

Currently, a static NGSI model,
the [smart data battery model](https://github.com/smart-data-models/dataModel.Battery/tree/master/Battery) is
implemented and will be transformed into a STA data structure.
This is represented in `de.frachtwerk.frost_ngsi_poc.model.Battery`.
Our goal is to viably store and load different information from the NGSI model into sensors, things, locations, etc. of
the STA model.

The NGSI model is split into several entities from the STA model and we only store certain properties of each type.
For example the `acPowerInput`and `acPowerOutput` of the battery is stored as a property of the corresponding Thing
entity.
Other data of the battery which is also categorized as part of the thing entity were omitted.
The location of the NGSI battery model is analog to the location of the STA model.
As a sensor the status of the battery is used to store a change in the status results in an observation in the STA data
structure.

### FROST

Our PoC stores and loads the data only in NGSI format.
Since we are using a FROST-Server this can be used to access the data showing the STA structure.

## Future work

This is only a simple proof of concept.

If a new NGSI data model should be implemented, the data must be manually mapped to the different STA entities.
Further development must work on automatically map the NGSI data to the correct STA entities.

## License

Copyright (C) 2023 Civitas Connect e.V., Hafenweg 7, D 48155 Münster.

This file is part of frost-ngsi-poc.

frost-ngsi-poc is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

frost-ngsi-poc is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with frost-ngsi-poc. If not, see <http://www.gnu.org/licenses/>.
