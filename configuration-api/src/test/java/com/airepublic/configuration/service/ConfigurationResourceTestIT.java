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
package com.airepublic.configuration.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Integrationtest for {@link ConfigurationServiceResource}.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class ConfigurationResourceTestIT {
    // private final RestClient client = new RestClient();
    private final static String REST_URI = "http://localhost:8080/ai-republic/configuration";
    private final Client client = ClientBuilder.newClient();


    @Test
    public void saveAndGetConfigurationTest() throws Exception {
        final TestConfiguration config = new TestConfiguration("testConfiguration", null, "blabla");
        Response response = client.target(REST_URI).path("save").request(MediaType.APPLICATION_JSON).post(Entity.entity(config, MediaType.APPLICATION_JSON));

        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());
        String json = response.readEntity(String.class);
        Assert.assertNotNull(json);

        response = doGet("testConfiguration", null);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        json = response.readEntity(String.class);
        Assert.assertNotNull(json);
        Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"\",\"test\":\"blabla\"}", json);

        doRemove("testConfiguration", null);
    }


    @Test
    public void saveConfigurationVariantTest() throws Exception {
        TestConfiguration config = new TestConfiguration("testConfiguration", "A", "aaa");
        Response response = doSave(config);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        final String result = response.readEntity(String.class);
        Assert.assertNotNull(result);

        config = new TestConfiguration("testConfiguration", "B", "bbb");
        response = doSave(config);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        response = doGet("testConfiguration", "A");
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        String json = response.readEntity(String.class);
        Assert.assertNotNull(json);
        Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"A\",\"test\":\"aaa\"}", json);

        response = doGet("testConfiguration", "B");
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        json = response.readEntity(String.class);
        Assert.assertNotNull(json);
        Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"B\",\"test\":\"bbb\"}", json);

        doRemove("testConfiguration", "A");
        doRemove("testConfiguration", "B");
    }


    @Test
    public void removeConfigurationTest() throws Exception {
        final TestConfiguration config = new TestConfiguration("testConfiguration", null, "blabla");
        Response response = doSave(config);

        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());
        String json = response.readEntity(String.class);
        Assert.assertNotNull(json);

        response = doGet("testConfiguration", null);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        json = response.readEntity(String.class);
        Assert.assertNotNull(json);
        Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"\",\"test\":\"blabla\"}", json);

        response = doRemove("testConfiguration", null);

        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        response = doGet("testConfiguration", null);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        json = response.readEntity(String.class);
        Assert.assertEquals("", json);

    }


    private Response doGet(final String id, final String variation) {
        final WebTarget webTarget = client.target("http://localhost:8080/ai-republic/");
        final WebTarget targetPath = webTarget.path("configuration/view?id=\" + id + \"&variation=\" + variation");
        final Invocation.Builder invocationBuilder = targetPath.request(MediaType.APPLICATION_JSON);

        final Response response = invocationBuilder.get();

        return response;
    }


    private Response doSave(final TestConfiguration config) throws JsonProcessingException {
        // final ObjectMapper mapper = new ObjectMapper();
        // final String json = mapper.writeValueAsString(config);

        final WebTarget webTarget = client.target("http://localhost:8080/ai-republic/");
        final WebTarget targetPath = webTarget.path("configuration/save");
        final Invocation.Builder invocationBuilder = targetPath.request(MediaType.APPLICATION_JSON);

        final Response response = invocationBuilder.post(Entity.entity(config, MediaType.APPLICATION_JSON));
        return response;
    }


    private Response doRemove(final String id, final String variation) {
        final WebTarget webTarget = client.target("http://localhost:8080/ai-republic/");
        final WebTarget targetPath = webTarget.path("configuration/remove?id=" + id + "&variation=" + variation);
        final Invocation.Builder invocationBuilder = targetPath.request(MediaType.APPLICATION_JSON);

        return invocationBuilder.delete();
    }
}
