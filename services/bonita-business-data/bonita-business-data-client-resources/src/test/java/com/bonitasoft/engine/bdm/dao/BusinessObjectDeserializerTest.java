package com.bonitasoft.engine.bdm.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.dao.BusinessObjectDeserializer;
import com.bonitasoft.engine.bdm.proxy.model.Child;

public class BusinessObjectDeserializerTest {

	private BusinessObjectDeserializer deserializer;

	@Before
	public void setUp() {
		deserializer = new BusinessObjectDeserializer();
	}
	
	@Test
	public void should_deserialize_an_entity() throws Exception {
		Child jules = new Child("jules", 1); 
		
		Child deserialized = deserializer.deserialize(jules.toJson().getBytes(), Child.class);
		
		assertThat(deserialized).isEqualTo(jules);
	}
	
	@Test
	public void should_deserialize_a_list_of_entities() throws Exception {
		Child jules = new Child("jules", 1);
		Child manon = new Child("manon", 0);
		String json = "[" + jules.toJson() + "," + manon.toJson() + "]";
		
		List<Child> deserialized = deserializer.deserializeList(json.getBytes(), Child.class);
		
		assertThat(deserialized).containsOnly(jules, manon);
	}
}
