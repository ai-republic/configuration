/**
   Copyright 2015 Torsten Oltmanns, ai-republic GmbH, Germany

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.airepublic.configuration.service.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.inject.Singleton;

import com.airepublic.configuration.api.ConfigurationServiceException;
import com.airepublic.configuration.api.ConfigurationServiceException.ErrorCode;
import com.airepublic.configuration.api.IConfiguration;
import com.airepublic.configuration.api.IConfigurationService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

/**
 * A service to manage and change configurations.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
@Singleton
@Default
@ConfigurationMongoQualifier
public class ConfigurationServiceMongo implements IConfigurationService, AutoCloseable {
    private final static String ID = "id";
    private final static String VARIATION = "variation";
    private MongoClient mongoClient;
    private DB db;
    private String configurationCollectionName;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public ConfigurationServiceMongo() {
        mapper.setSerializationInclusion(Include.ALWAYS);
    }


    @PostConstruct
    protected void initialize() throws IOException {
        // read mongo configuration
        final ResourceBundle props = ResourceBundle.getBundle("mongo");

        // initialize mongo client
        String serverStringList = props.getString("mongo.servers");

        if (serverStringList == null || serverStringList.isBlank()) {
            serverStringList = "localhost";
        }

        final List<String> servers = Arrays.asList(serverStringList.split(","));
        final List<ServerAddress> seeds = new ArrayList<>();

        for (final String server : servers) {
            final int idx = server.indexOf(":");

            if (idx != -1) {
                final String host = server.substring(0, idx);
                final int port = Integer.parseInt(server.substring(idx + 1));
                seeds.add(new ServerAddress(host, port));
            } else {
                seeds.add(new ServerAddress(server));
            }
        }

        mongoClient = new MongoClient(seeds);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    mongoClient.close();
                } catch (final Exception e) {
                }
            }
        });

        final String configurationDbName = props.getString("mongo.db.configurations");
        db = mongoClient.getDB(configurationDbName);

        configurationCollectionName = props.getString("mongo.collection.configurations");
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConfiguration(final String id, String variation, final Class<? extends IConfiguration> clazz) throws ConfigurationServiceException {
        try {
            if (variation == null) {
                variation = "";
            }

            final BasicDBObject ref = new BasicDBObject(ID, id);
            ref.put(VARIATION, variation);

            final DBObject obj = db.getCollection(configurationCollectionName).findOne(ref);

            if (obj != null) {
                obj.removeField("_id");

                return (T) mapper.readValue(obj.toString(), clazz);
            }

            return tryAndLoadProperties(id, variation, clazz);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error reading configuration '" + id + "'!", e);
        }
    }


    @Override
    public String getConfigurationAsJson(final String id, String variation) throws ConfigurationServiceException {
        try {
            if (variation == null) {
                variation = "";
            }

            final BasicDBObject ref = new BasicDBObject(ID, id);
            ref.put(VARIATION, variation);

            final DBObject obj = db.getCollection(configurationCollectionName).findOne(ref);

            if (obj != null) {
                obj.removeField("_id");

                return obj.toString();
            }

            return null;
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error reading configuration '" + id + "'!", e);
        }
    }


    @Override
    public void saveConfiguration(final IConfiguration configuration) throws ConfigurationServiceException {
        try {
            final String json = mapper.writeValueAsString(configuration);
            final DBObject obj = (DBObject) JSON.parse(json);

            final BasicDBObject query = new BasicDBObject(ID, configuration.getId());

            if (configuration.getVariation() != null) {
                query.put(VARIATION, configuration.getVariation());
            }

            db.getCollection(configurationCollectionName).update(query, obj, true, false);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_SAVE, "Error saving configuration '" + configuration.getId() + "'!", e);
        }
    }


    @Override
    public void saveConfigurationAsJson(final String configurationJson) throws ConfigurationServiceException {
        try {
            final DBObject obj = (DBObject) JSON.parse(configurationJson);
            final String id = (String) obj.get(ID);
            final String variation = (String) obj.get(VARIATION);

            final BasicDBObject query = new BasicDBObject(ID, id);

            if (variation != null) {
                query.put(VARIATION, variation);
            }

            db.getCollection(configurationCollectionName).update(query, obj, true, false);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_SAVE, "Error saving configuration '" + configurationJson + "'!", e);
        }
    }


    @Override
    public void removeConfiguration(final String id, final String variation) throws ConfigurationServiceException {
        try {
            final BasicDBObject ref = new BasicDBObject(ID, id);
            ref.put(VARIATION, variation);

            db.getCollection(configurationCollectionName).remove(ref);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error removing configuration '" + id + "'!", e);

        }
    }


    @SuppressWarnings("unchecked")
    protected <T> T tryAndLoadProperties(final String id, final String variation, final Class<? extends IConfiguration> clazz) {
        try {
            final IConfiguration t = clazz.getDeclaredConstructor().newInstance();
            t.setId(id);
            t.setVariation(variation);

            t.resetToDefault();

            saveConfiguration(t);

            return (T) t;
        } catch (final Exception e) {
        }
        return null;
    }


    @Override
    @PreDestroy
    public void close() {
        mongoClient.close();
    }


    /**
     * @return the mongoClient
     */
    protected MongoClient getMongoClient() {
        return mongoClient;
    }


    /**
     * @param mongoClient the mongoClient to set
     */
    protected void setMongoClient(final MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    /**
     * @return the db
     */
    protected DB getDb() {
        return db;
    }


    /**
     * @param db the db to set
     */
    protected void setDb(final DB db) {
        this.db = db;
    }


    /**
     * @return the configurationCollectionName
     */
    protected String getConfigurationCollectionName() {
        return configurationCollectionName;
    }


    /**
     * @param configurationCollectionName the configurationCollectionName to set
     */
    protected void setConfigurationCollectionName(final String configurationCollectionName) {
        this.configurationCollectionName = configurationCollectionName;
    }
}
