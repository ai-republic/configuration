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

import org.apache.commons.lang3.StringUtils;

/**
 * An abstract configuration implementing the basics of the {@link IConfiguration} interface.
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public abstract class AbstractConfiguration implements IConfiguration {
	private static final long serialVersionUID = -2003949602323748793L;
	private String id;
	private String variation;
	
	/**
	 * Default constructor.
	 */
	public AbstractConfiguration() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param id the id
	 * @param variation the variation
	 */
	public AbstractConfiguration(String id, String variation) {
		this.id = id;
		this.variation = variation;
	}
	
	@Override
	public abstract void resetToDefault() throws ConfigurationServiceException;
	
	
	/**
	 * Builds the name for the current variation, i.e. &lt;id&gt;-&lt;variation&gt;.
	 * If no variation is set, null or empty string, then only the id is returned.
	 * 
	 * @return the variation name
	 */
	protected String buildVariationName() {
		String bundleName = getId();
			
		if (StringUtils.isNotBlank(variation)) {
			bundleName += "-" + getVariation();
		}
		
		return bundleName;
	}
	
	@Override
	public final String getId() {
		return id;
	}
	
	@Override
	public final void setId(String id) {
		this.id = id;
	}

	@Override
	public final String getVariation() {
		if (StringUtils.isNotBlank(variation)) {
			return variation;
		}
		
		return "";
	}

	@Override
	public final void setVariation(String variation) {
		this.variation = variation;
	}
}
