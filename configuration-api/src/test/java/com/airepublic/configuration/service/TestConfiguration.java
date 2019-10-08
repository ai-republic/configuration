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

import java.util.ResourceBundle;

import com.airepublic.configuration.api.AbstractConfiguration;
import com.airepublic.configuration.api.ConfigurationServiceException;
import com.airepublic.configuration.api.ConfigurationServiceException.ErrorCode;

public class TestConfiguration extends AbstractConfiguration {
	private static final long serialVersionUID = -1774027215218292123L;
	private String test;
	
	public TestConfiguration() {
	}
	
	public TestConfiguration(String id, String variation, String test) {
		super(id, variation);
		this.test = test;
	}
	
	@Override
	public void resetToDefault() throws ConfigurationServiceException {
		try {
			ResourceBundle bundle= ResourceBundle.getBundle(buildVariationName());
			
			setTest(bundle.getString("test"));
		}
		catch (Exception e) {
			throw new ConfigurationServiceException(ErrorCode.ERROR_LOADING_DEFAULT, "Couldn't load configuration resource: " + buildVariationName(), e);
		}
	}
	
	public void setTest(String test) {
		this.test = test;
	}
	
	public String getTest() {
		return test;
	}
}
