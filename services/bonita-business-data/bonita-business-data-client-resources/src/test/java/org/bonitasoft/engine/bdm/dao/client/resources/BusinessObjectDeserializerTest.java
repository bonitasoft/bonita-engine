/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm.dao.client.resources;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.bdm.proxy.model.Child;
import org.junit.Before;
import org.junit.Test;

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
