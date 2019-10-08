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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airepublic.configuration.api.IConfigurationService;
import com.airepublic.exception.ExceptionInterceptor;

/**
 * REST webservice to access and manage configurations.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
@Path("configuration")
@Interceptors(ExceptionInterceptor.class)
@RequestScoped
public class ConfigurationServiceResource {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceResource.class);

    @Inject
    private IConfigurationService service;

    @Context
    private SecurityContext securityContext;


    /**
     * Gets the configuration as Json.
     * 
     * @param id the configuration id
     * @param variation the variation option
     * @return the configuration as a pretty-printed JSON or an empty JSON if not loaded
     */
    @GET
    @Path("/view")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfiguration(@QueryParam("id") final String id, @QueryParam("variation") String variation) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // throw new SecurityException("Access denied!");
        }

        if (variation != null && variation.equals("null")) {
            variation = null;
        }

        try {
            final String json = service.getConfigurationAsJson(id, variation);
            final Response response = Response.ok().entity(json).build();
            return response;
        } catch (final Exception e) {
            LOG.error("Error reading configuration '" + id + "'!", e);
        }

        return Response.status(Status.BAD_REQUEST).build();
    }


    /**
     * Saves the specified configuration provided as JSON for the specified id.
     * 
     * @param id the the configuration id
     * @param configJson the configuration as JSON
     * @param className the qualified class name of the configuration class
     * @return true if saved successfully, otherwise false
     */
    @POST
    @Path("/save")
    public Response saveConfiguration(final String json) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // throw new SecurityException("Access denied!");
        }

        try {
            service.saveConfigurationAsJson(json);
        } catch (final Exception e) {
            LOG.error("Error saving configuration '" + json + "'!", e);
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }


    /**
     * Removes the configuration for the specified id.
     * 
     * @param id the the configuration id
     * @param variation the variation option
     * @return true if removed successfully, otherwise false
     */
    @DELETE
    @Path("/remove")
    public Response removeConfiguration(@QueryParam("id") final String id, @QueryParam("variation") String variation) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            // throw new SecurityException("Access denied!");
        }

        if (variation != null && variation.equals("null")) {
            variation = null;
        }

        try {
            service.removeConfiguration(id, variation);
        } catch (final Exception e) {
            LOG.error("Error removing configuration '" + id + "'!", e);
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }
}
