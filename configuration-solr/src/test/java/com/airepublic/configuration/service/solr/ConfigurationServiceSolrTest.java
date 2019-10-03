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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.airepublic.configuration.service.solr.TestConfiguration;

/**
 * Unittest for {@link ConfigurationServiceMongo}
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class ConfigurationServiceSolrTest {
	private ConfigurationServiceSolr service = new ConfigurationServiceSolr();
	private HttpSolrServer mockServer = mock(HttpSolrServer.class);
	
	@Before
	public void setUp() {
		service.setSolrServer(mockServer);
	}
	
	@Test
	public void saveConfigurationTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "hello");
		when(mockServer.add(any(SolrInputDocument.class))).then(new Answer<SolrInputDocument>() {
			@Override
			public SolrInputDocument answer(InvocationOnMock invocation) throws Throwable {
				SolrInputDocument value = (SolrInputDocument)invocation.getArguments()[0];
				Assert.assertEquals("testConfiguration", value.getFieldValue("id"));
				Assert.assertEquals("", value.getFieldValue("variation"));
				Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"\",\"test\":\"hello\"}", value.getFieldValue("configuration"));
				return null;
			}
		});
		service.saveConfiguration(config);
	}

	@Test
	public void saveConfigurationAsJsonTest() throws Exception {
		when(mockServer.add(any(SolrInputDocument.class))).then(new Answer<SolrInputDocument>() {
			@Override
			public SolrInputDocument answer(InvocationOnMock invocation) throws Throwable {
				SolrInputDocument value = (SolrInputDocument)invocation.getArguments()[0];
				Assert.assertEquals("testConfiguration", value.getFieldValue("id"));
				Assert.assertEquals("", value.getFieldValue("variation"));
				Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":\"\",\"test\":\"helloAgain\"}", value.getFieldValue("configuration"));
				return null;
			}
		});
		service.saveConfigurationAsJson("{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}");
	}

	@Test
	public void getConfigurationTest() throws Exception {
		SolrDocument doc = new SolrDocument();
		doc.addField("id", "testConfiguration");
		doc.addField("variation", null);
		doc.addField("configuration", "{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}");
		SolrDocumentList docList = new SolrDocumentList();
		docList.add(doc);
		QueryResponse response = mock(QueryResponse.class);
		when(response.getResults()).thenReturn(docList);
		when(mockServer.query(any(SolrQuery.class))).thenReturn(response);
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("testConfiguration", chk.getId());
		Assert.assertEquals("", chk.getVariation());
		Assert.assertEquals("helloAgain", chk.getTest());
	}
	
	@Test
	public void getConfigurationAsJsonTest() throws Exception {
		SolrDocument doc = new SolrDocument();
		doc.addField("id", "testConfiguration");
		doc.addField("variation", null);
		doc.addField("configuration", "{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}");
		SolrDocumentList docList = new SolrDocumentList();
		docList.add(doc);
		QueryResponse response = mock(QueryResponse.class);
		when(response.getResults()).thenReturn(docList);
		when(mockServer.query(any(SolrQuery.class))).thenReturn(response);
		
		String chk = service.getConfigurationAsJson("testConfiguration", null);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("{\"id\":\"testConfiguration\",\"variation\":null,\"test\":\"helloAgain\"}", chk);
	}
	
	@Test
	public void removeConfigurationTest() throws Exception {
		when(mockServer.deleteByQuery(any(String.class))).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				String value = (String)invocation.getArguments()[0];
				Assert.assertEquals("id:\"testConfiguration\" AND variation:\"\"", value);
				return null;
			}
		});
		service.removeConfiguration("testConfiguration", null);
	}
	
	@Test
	public void resetToDefaultTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "hello");
		
		Assert.assertEquals("hello", config.getTest());
		
		config.resetToDefault();
		
		Assert.assertEquals("Hello world", config.getTest());
		
		config.setVariation("A");
		config.resetToDefault();
		
		Assert.assertEquals("Hello world A", config.getTest());
		
		config.setVariation("B");
		config.resetToDefault();
		
		Assert.assertEquals("Hello world B", config.getTest());
	}
}
