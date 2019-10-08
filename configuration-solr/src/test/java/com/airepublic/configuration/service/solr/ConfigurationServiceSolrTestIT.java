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

import org.junit.Assert;
import org.junit.Test;

import com.airepublic.configuration.service.solr.TestConfiguration;

/**
 * Integrationtest for {@link ConfigurationServiceSolr}
 * 
 * @author Torsten.Oltmanns@comvel.de, Torsten.Oltmanns@ai-republic.com
 */
public class ConfigurationServiceSolrTestIT {
	
	@Test
	public void saveAndGetConfigurationTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "hello");
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfiguration(config);
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals(config.getId(), chk.getId());
		Assert.assertEquals(config.getVariation(), chk.getVariation());
		Assert.assertEquals(config.getTest(), chk.getTest());
		
		service.removeConfiguration(chk.getId(), chk.getVariation());
	}
	
	@Test
	public void updateConfigurationTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "hello");
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfiguration(config);
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals(config.getTest(), chk.getTest());
		
		config.setTest("blabla");
		
		service.saveConfiguration(config);
		
		chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals(config.getTest(), chk.getTest());
		
		service.removeConfiguration(chk.getId(), chk.getVariation());
	}

	@Test
	public void saveConfigurationAsJsonTest() throws Exception {
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfigurationAsJson("{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}");
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("testConfiguration", chk.getId());
		Assert.assertEquals("", chk.getVariation());
		Assert.assertEquals("helloAgain", chk.getTest());
		
		service.removeConfiguration(chk.getId(), chk.getVariation());
	}
	
	@Test
	public void removeConfigurationTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "test");
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfiguration(config);
		
		service.removeConfiguration(config.getId(), config.getVariation());
		
		TestConfiguration chk = service.getConfiguration(config.getId(), config.getVariation(), TestConfiguration.class);
		
		//check for default
		Assert.assertNotNull(chk);
		Assert.assertEquals("Hello world", chk.getTest());
	}
	
	@Test
	public void configurationVariationTest() throws Exception {
		TestConfiguration configA = new TestConfiguration("testConfiguration", "A", "aaa");
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfiguration(configA);
		
		TestConfiguration configB = new TestConfiguration("testConfiguration", "B", "bbb");
		service.saveConfiguration(configB);

		TestConfiguration chk = service.getConfiguration("testConfiguration", "A", TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals(configA.getId(), chk.getId());
		Assert.assertEquals(configA.getVariation(), chk.getVariation());
		Assert.assertEquals(configA.getTest(), chk.getTest());
		
		//change config A
		chk.setTest("123");
		service.saveConfiguration(chk);
		
		chk = service.getConfiguration("testConfiguration", "B", TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals(configB.getId(), chk.getId());
		Assert.assertEquals(configB.getVariation(), chk.getVariation());
		Assert.assertEquals(configB.getTest(), chk.getTest());

		//change config B
		chk.setTest("789");
		service.saveConfiguration(chk);

		service.removeConfiguration("testConfiguration", "A");
		
		//check for default
		chk = service.getConfiguration("testConfiguration", "A", TestConfiguration.class);
		Assert.assertNotNull(chk);
		Assert.assertEquals("Hello world A", chk.getTest());
		
		service.removeConfiguration("testConfiguration", "B");
		
		chk = service.getConfiguration("testConfiguration", "B", TestConfiguration.class);
		Assert.assertNotNull(chk);
		Assert.assertEquals("Hello world B", chk.getTest());
	}
	
	@Test
	public void getConfigurationTest() throws Exception {
		ConfigurationServiceSolr service = new ConfigurationServiceSolr();
		service.initialize();
		
		service.saveConfigurationAsJson("{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}");
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("testConfiguration", chk.getId());
		Assert.assertEquals("", chk.getVariation());
		Assert.assertEquals("helloAgain", chk.getTest());
		
		service.removeConfiguration(chk.getId(), chk.getVariation());
	}

}
