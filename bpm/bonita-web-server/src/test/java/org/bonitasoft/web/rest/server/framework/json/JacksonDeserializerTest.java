/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.framework.json;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.json.model.Address;
import org.bonitasoft.web.rest.server.framework.json.model.User;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;

/** extends APITestWithMock to avoid NullPointerException on I18n */
public class JacksonDeserializerTest extends APITestWithMock {

    private JacksonDeserializer jacksonDeserializer;

    @Before
    public void initializeDeserializer() {
        jacksonDeserializer = new JacksonDeserializer();
    }

    @Test(expected = APIException.class)
    public void deserialize_throw_exception_if_json_is_non_well_formed() {
        String nonWellFormedJson = "someJsonNonWellFormedJson";

        jacksonDeserializer.deserialize(nonWellFormedJson, String.class);
    }

    @Test(expected = APIException.class)
    public void deserialize_throw_exception_if_mapping_between_class_and_json_is_incorrect() {
        String notUserJson = "{\"unknownUserAttribute\": \"unknownAttributeValue\"}";

        jacksonDeserializer.deserialize(notUserJson, User.class);
    }

    @Test
    public void deserialize_can_deserialize_primitives_types() {

        Long deserializedLong = jacksonDeserializer.deserialize("1", Long.class);

        assertThat(deserializedLong, is(1L));
    }

    @Test
    public void deserialize_can_deserialize_complex_types() {
        User expectedUser = new User(1, "Colin", "Puy", new Date(428558400000L),
                new Address("310 La Gouterie", "Charnecles"));
        String json = "{\"address\":{\"street\":\"310 La Gouterie\",\"city\":\"Charnecles\"},\"id\":1,\"firstName\":\"Colin\",\"lastName\":\"Puy\",\"birthday\":428558400000}";

        User deserializedUser = jacksonDeserializer.deserialize(json, User.class);

        assertThat(deserializedUser, equalTo(expectedUser));
    }

    @Test(expected = APIException.class)
    public void deserializeList_throw_exception_if_json_is_not_a_list() {
        String json = "{\"address\":{\"street\":\"310 La Gouterie\",\"city\":\"Charnecles\"},\"id\":1,\"firstName\":\"Colin\",\"lastName\":\"Puy\",\"birthday\":428558400000}";

        jacksonDeserializer.deserializeList(json, User.class);
    }

    @Test
    public void deserializeList_can_deserialize_primitives_types() {

        List<Long> longs = jacksonDeserializer.deserializeList("[1, 2, 3]", Long.class);

        assertThat(longs, hasItems(1L, 2L, 3L));
    }

    @Test
    public void deserializeList_can_deserialize_list_of_complex_type() {
        User expectedUser1 = new User(1, "Colin", "Puy", new Date(428558400000L),
                new Address("310 La Gouterie", "Charnecles"));
        User expectedUser2 = new User(2, "Clara", "Morgan", new Date(349246800000L),
                new Address("somewhere i don't know", "Paris"));
        String json = "["
                +
                "{\"address\":{\"city\":\"Charnecles\",\"street\":\"310 La Gouterie\"},\"id\":1,\"firstName\":\"Colin\",\"lastName\":\"Puy\",\"birthday\":428558400000},"
                +
                "{\"address\":{\"city\":\"Paris\",\"street\":\"somewhere i don't know\"},\"id\":2,\"firstName\":\"Clara\",\"lastName\":\"Morgan\",\"birthday\":349246800000}"
                +
                "]";

        List<User> users = jacksonDeserializer.deserializeList(json, User.class);

        assertThat(users, hasItem(expectedUser1));
        assertThat(users, hasItem(expectedUser2));
    }
}
