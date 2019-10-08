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
package com.airepublic.configuration.api;

/**
 * A service interface to manage configurations.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public interface IConfigurationService {

	/**
	 * Gets the configuration for the specified name.
	 * 
	 * @param id the unique identifier
	 * @param variation the variation option
	 * @param clazz the configuration class to return
	 * @return the configuration or null if not found
	 * @throws ConfigurationServiceException (with ErrorCode.ERROR_GET) if an error occurred during
	 *         reading
	 */
	<T> T getConfiguration(String id, String variation, Class<? extends IConfiguration> clazz) throws ConfigurationServiceException;


	/**
	 * Gets the configuration as Json for the specified name.
	 * 
	 * @param id the unique identifier
	 * @param variation the variation option
	 * @return the configuration as Json or null if not found
	 * @throws ConfigurationServiceException (with ErrorCode.ERROR_GET) if an error occurred during
	 *         reading
	 */
	String getConfigurationAsJson(String id, String variation) throws ConfigurationServiceException;


	/**
	 * Saves or updates the specified configuration.
	 * 
	 * @param configuration the configuration
	 * @throws ConfigurationServiceException (with ErrorCode.ERROR_SAVE) if an error occurred during
	 *         saving
	 */
	void saveConfiguration(IConfiguration configuration) throws ConfigurationServiceException;


	/**
	 * Saves or updates the specified JSON configuration.
	 * 
	 * @param configuration the configuration as JSON
	 * @throws ConfigurationServiceException (with ErrorCode.ERROR_SAVE) if an error occurred during
	 *         saving
	 */
	void saveConfigurationAsJson(String configuration) throws ConfigurationServiceException;


	/**
	 * Removes the specified configuration.
	 * 
	 * @param id the unique identifier
	 * @param variation the variation option
	 * @throws ConfigurationServiceException (with ErrorCode.ERROR_REMOVE) if an error occurred
	 *         during removing
	 */
	void removeConfiguration(String id, String variation) throws ConfigurationServiceException;
}