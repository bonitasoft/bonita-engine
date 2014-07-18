package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.bdm.proxy.model.Child;

public class BusinessObjectDeserializerTest {

	@Test
	public void should_deserialize_an_Entity() throws Exception {
		Child jules = new Child("jules", 1); 
		
		Child deserialized = new BusinessObjectDeserializer().deserialize(jules.toJson().getBytes(), Child.class);
		
		assertThat(deserialized).isEqualTo(jules);
	}
	
	@Test
	public void should_deserialize_a_List_of_Entities() throws Exception {
		Child jules = new Child("jules", 1);
		Child manon = new Child("manon", 0);
		String json = "[" + jules.toJson() + "," + manon.toJson() + "]";
		
		List<Child> deserialized = new BusinessObjectDeserializer().deserializeList(json.getBytes(), Child.class);
		
		assertThat(deserialized).containsOnly(jules, manon);
	}
}
