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
package com.airepublic.configuration.service.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.airepublic.configuration.api.ConfigurationServiceException;
import com.airepublic.configuration.api.ConfigurationServiceException.ErrorCode;
import com.airepublic.configuration.api.IConfiguration;
import com.airepublic.configuration.api.IConfigurationService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A service to manage and change configurations.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
@Singleton
@ConfigurationSolrQualifier
public class ConfigurationServiceSolr implements IConfigurationService {
    private final static String ID = "id";
    private final static String VARIATION = "variation";
    private final static String CONFIGURATION = "configuration";
    private final static long CACHE_REFRESH_INTERVAL = 3600000L; // one hour
    private HttpSolrServer solrServer;
    private final ObjectMapper mapper = new ObjectMapper();
    private long currentTime = System.currentTimeMillis();
    private final Map<String, IConfiguration> cache = new HashMap<>();


    /**
     * Constructor.
     */
    public ConfigurationServiceSolr() {
        mapper.setSerializationInclusion(Include.ALWAYS);
    }


    @PostConstruct
    public void initialize() throws IOException {
        // read solr configuration
        final ResourceBundle props = ResourceBundle.getBundle("solr");
        final String baseUrl = props.getString("solr.base.url.configuration");

        solrServer = new HttpSolrServer(baseUrl);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getConfiguration(final String id, String variation, final Class<? extends IConfiguration> clazz) throws ConfigurationServiceException {
        IConfiguration c;

        try {
            if (variation == null) {
                variation = "";
            }

            if (System.currentTimeMillis() - currentTime > CACHE_REFRESH_INTERVAL) {
                currentTime = System.currentTimeMillis();
                cache.clear();
            }

            c = cache.get(id + "_" + variation);

            if (c == null) {
                final SolrQuery query = new SolrQuery();
                query.setQuery(ID + ":\"" + id + "\" AND " + VARIATION + ":\"" + variation + "\"");

                final QueryResponse response = solrServer.query(query);
                final SolrDocumentList list = response.getResults();

                if (list.size() > 0) {
                    final SolrDocument doc = list.get(0);

                    final String json = (String) doc.getFieldValue("configuration");
                    c = mapper.readValue(json, clazz);

                    cache.put(id + "_" + variation, c);
                } else {
                    c = tryAndLoadProperties(id, variation, clazz);
                    cache.put(id + "_" + variation, c);
                }
            }
        } catch (final Exception e) {
            try {
                c = tryAndLoadProperties(id, variation, clazz);
                cache.put(id + "_" + variation, c);
            } catch (final Exception e1) {
                throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error reading configuration \"" + id + "\"!", e);
            }
        }

        return (T) c;
    }


    @Override
    public String getConfigurationAsJson(final String id, String variation) throws ConfigurationServiceException {
        try {
            if (variation == null) {
                variation = "";
            }

            if (System.currentTimeMillis() - currentTime > CACHE_REFRESH_INTERVAL) {
                currentTime = System.currentTimeMillis();
                cache.clear();
            }

            final IConfiguration c = cache.get(id + "_" + variation);

            if (c == null) {
                final SolrQuery query = new SolrQuery();
                query.setQuery(ID + ":\"" + id + "\" AND " + VARIATION + ":\"" + variation + "\"");

                final QueryResponse response = solrServer.query(query);
                final SolrDocumentList list = response.getResults();

                if (list.size() > 0) {
                    final SolrDocument doc = list.get(0);

                    return (String) doc.getFieldValue("configuration");
                }
            } else {
                return mapper.writeValueAsString(c);
            }

            return null;
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error reading configuration \"" + id + "\"!", e);
        }
    }


    @Override
    public void saveConfiguration(final IConfiguration configuration) throws ConfigurationServiceException {
        try {
            if (configuration.getVariation() == null) {
                configuration.setVariation("");
            }

            final String json = mapper.writeValueAsString(configuration);

            solrServer.deleteByQuery(ID + ":\"" + configuration.getId() + "\" AND " + VARIATION + ":\"" + configuration.getVariation() + "\"");

            final SolrInputDocument doc = new SolrInputDocument();
            doc.addField(ID, configuration.getId());
            doc.addField(VARIATION, configuration.getVariation());
            doc.addField(CONFIGURATION, json);

            solrServer.add(doc);
            solrServer.commit();

            cache.put(configuration.getId() + "_" + configuration.getVariation(), configuration);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_SAVE, "Error saving configuration \"" + configuration.getId() + "\"!", e);
        }
    }


    @Override
    public void saveConfigurationAsJson(final String configurationJson) throws ConfigurationServiceException {
        try {
            final JsonNode node = mapper.readTree(configurationJson);
            final String id = node.get(ID).textValue();
            String variation = node.get(VARIATION).textValue();

            if (variation == null) {
                variation = "";
                ((ObjectNode) node).put(VARIATION, "");
            }

            solrServer.deleteByQuery(ID + ":\"" + id + "\" AND " + VARIATION + ":\"" + variation + "\"");

            final SolrInputDocument doc = new SolrInputDocument();
            doc.addField(ID, id);
            doc.addField(VARIATION, variation);
            doc.addField(CONFIGURATION, node.toString());

            solrServer.add(doc);
            solrServer.commit();

            cache.remove(id + "_" + variation);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_SAVE, "Error saving configuration \"" + configurationJson + "\"!", e);
        }
    }


    @Override
    public void removeConfiguration(final String id, String variation) throws ConfigurationServiceException {
        try {
            if (variation == null) {
                variation = "";
            }

            solrServer.deleteByQuery(ID + ":\"" + id + "\" AND " + VARIATION + ":\"" + variation + "\"");
            solrServer.commit();

            cache.remove(id + "_" + variation);
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error removing configuration \"" + id + "\"!", e);

        }
    }


    @SuppressWarnings("unchecked")
    protected <T> T tryAndLoadProperties(final String id, final String variation, final Class<? extends IConfiguration> clazz) throws ConfigurationServiceException {
        try {
            final IConfiguration t = clazz.newInstance();
            t.setId(id);
            t.setVariation(variation);

            t.resetToDefault();

            saveConfiguration(t);

            return (T) t;
        } catch (final Exception e) {
            throw new ConfigurationServiceException(ErrorCode.ERROR_GET, "Error loading default configuration for " + clazz.getSimpleName(), e);
        }
    }


    @Override
    protected void finalize() throws Throwable {
        solrServer.shutdown();
        super.finalize();
    }


    /**
     * @return the solrServer
     */
    protected HttpSolrServer getSolrServer() {
        return solrServer;
    }


    /**
     * @param solrServer the solrServer to set
     */
    protected void setSolrServer(final HttpSolrServer solrServer) {
        this.solrServer = solrServer;
    }
}
