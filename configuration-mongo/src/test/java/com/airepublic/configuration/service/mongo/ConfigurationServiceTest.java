/**
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.airepublic.configuration.service.mongo.TestConfiguration;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/**
 * Unittest for {@link ConfigurationServiceMongo}
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class ConfigurationServiceTest {
	private ConfigurationServiceMongo service = new ConfigurationServiceMongo();
	private DBCollection mockCol = mock(DBCollection.class);
	private DB mockDB = mock(DB.class); 
	
	@Before
	public void setUp() {
		service.setConfigurationCollectionName("configurationCollection");
		service.setMongoClient(mock(MongoClient.class));
		service.setDb(mockDB);
		when(mockDB.getCollection(any(String.class))).thenReturn(mockCol);
	}
	
	@Test
	public void saveConfigurationTest() throws Exception {
		TestConfiguration config = new TestConfiguration("testConfiguration", null, "hello");
		when(mockCol.update(any(DBObject.class), any(DBObject.class), any(Boolean.class), any(Boolean.class))).then(new Answer<DBObject>() {
			@Override
			public DBObject answer(InvocationOnMock invocation) throws Throwable {
				DBObject value = (DBObject)invocation.getArguments()[1];
				Assert.assertEquals("testConfiguration", value.get("id"));
				Assert.assertEquals("", value.get("variation"));
				Assert.assertEquals("hello", value.get("test"));
				return null;
			}
		});
		service.saveConfiguration(config);
	}

	@Test
	public void saveConfigurationAsJsonTest() throws Exception {
		when(mockCol.update(any(DBObject.class), any(DBObject.class), any(Boolean.class), any(Boolean.class))).then(new Answer<DBObject>() {
			@Override
			public DBObject answer(InvocationOnMock invocation) throws Throwable {
				DBObject value = (DBObject)invocation.getArguments()[1];
				Assert.assertEquals("testConfiguration", value.get("id"));
				Assert.assertEquals(null, value.get("variation"));
				Assert.assertEquals("helloAgain", value.get("test"));
				return null;
			}
		});
		service.saveConfigurationAsJson("{ id : \"testConfiguration\" , variation : null , test : \"helloAgain\" }");
	}

	@Test
	public void getConfigurationTest() throws Exception {
		when(mockCol.findOne(any(DBObject.class))).thenReturn((DBObject)JSON.parse("{_id:\"asdfasdf\", id:\"testConfiguration\", variation:null, test:\"helloAgain\"}"));
		
		TestConfiguration chk = service.getConfiguration("testConfiguration", null, TestConfiguration.class);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("testConfiguration", chk.getId());
		Assert.assertEquals("", chk.getVariation());
		Assert.assertEquals("helloAgain", chk.getTest());
	}
	
	@Test
	public void getConfigurationAsJsonTest() throws Exception {
		when(mockCol.findOne(any(DBObject.class))).thenReturn((DBObject)JSON.parse("{_id:\"asdfasdf\", id:\"testConfiguration\", variation:null, test:\"helloAgain\"}"));
		
		String chk = service.getConfigurationAsJson("testConfiguration", null);
		
		Assert.assertNotNull(chk);
		Assert.assertEquals("{ \"id\" : \"testConfiguration\" , \"variation\" :  null  , \"test\" : \"helloAgain\"}", chk);
	}
	
	@Test
	public void removeConfigurationTest() throws Exception {
		when(mockCol.remove(any(DBObject.class))).then(new Answer<DBObject>() {
			@Override
			public DBObject answer(InvocationOnMock invocation) throws Throwable {
				DBObject value = (DBObject)invocation.getArguments()[0];
				Assert.assertEquals("testConfiguration", value.get("id"));
				Assert.assertEquals(null, value.get("variation"));
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
	
	@Test
	public void initialLoadingTest() throws Exception {
		TestConfiguration config = service.getConfiguration("testConfiguration", "A", TestConfiguration.class);
		
		Assert.assertEquals("Hello world A", config.getTest());
	}
}
