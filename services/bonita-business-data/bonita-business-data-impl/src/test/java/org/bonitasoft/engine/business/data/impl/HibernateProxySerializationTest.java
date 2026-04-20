/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.company.model.Address;
import org.bonitasoft.engine.bdm.Entity;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Test;

/**
 * Verifies JSON serialization behavior for BDM entities when Hibernate returns
 * {@link HibernateProxy} instances for relationships or top-level entities.
 * <p>
 * Initialized proxies are unwrapped so their fields serialize as if they were real entities,
 * while uninitialized proxies remain opaque to avoid triggering lazy loading.
 */
public class HibernateProxySerializationTest {

    private static final String PARAMETER_BUSINESSDATA_CLASS_URI_VALUE = "/businessdata/{className}/{id}/{field}";

    private final JsonBusinessDataSerializerImpl serializer = new JsonBusinessDataSerializerImpl();

    /**
     * An initialized {@link HibernateProxy} child entity serializes with all its fields.
     */
    @Test
    public void serializeEntity_should_include_child_fields_when_child_is_hibernate_proxy() throws Exception {
        // given
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(proxyAddress);

        // when
        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then - all fields should be present, including child entity fields
        assertThatJson(json).node("persistenceId").isEqualTo(1);
        assertThatJson(json).node("name").isEqualTo("John Doe");
        assertThatJson(json).node("address.persistenceId").isEqualTo(123);
        assertThatJson(json).node("address.street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("address.number").isEqualTo(32.0);
    }

    /**
     * A non-proxy child entity serializes with all its fields.
     */
    @Test
    public void serializeEntity_should_include_all_fields_when_child_is_not_proxy() throws Exception {
        // given
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(realAddress);

        // when
        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then - all fields should be present
        assertThatJson(json).node("persistenceId").isEqualTo(1);
        assertThatJson(json).node("name").isEqualTo("John Doe");
        assertThatJson(json).node("address.persistenceId").isEqualTo(123);
        assertThatJson(json).node("address.street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("address.number").isEqualTo(32.0);
    }

    /**
     * Manually unwrapping a {@link HibernateProxy} before serialization produces the
     * same result as letting the serializer unwrap it.
     */
    @Test
    public void serializeEntity_should_include_all_fields_when_proxy_is_manually_unwrapped() throws Exception {
        // given
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        // Unwrap the proxy manually
        Entity unwrappedAddress = unwrapProxy(proxyAddress);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(unwrappedAddress);

        // when
        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then - all fields should be present when proxy is unwrapped
        assertThatJson(json).node("persistenceId").isEqualTo(1);
        assertThatJson(json).node("name").isEqualTo("John Doe");
        assertThatJson(json).node("address.persistenceId").isEqualTo(123);
        assertThatJson(json).node("address.street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("address.number").isEqualTo(32.0);
    }

    /**
     * A top-level {@link HibernateProxy} entity serializes with all its fields.
     */
    @Test
    public void serializeEntity_should_include_all_fields_when_entity_is_hibernate_proxy() throws Exception {
        // given
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        // when
        String json = serializer.serializeEntity(proxyAddress, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // then - all fields should be present
        assertThatJson(json).node("persistenceId").isEqualTo(123);
        assertThatJson(json).node("persistenceVersion").isEqualTo(1);
        assertThatJson(json).node("street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("number").isEqualTo(32.0);
    }

    @Test
    public void serializeEntity_should_not_unwrap_lazy_uninitialized_proxy() throws Exception {
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        AddressHibernateProxy lazyProxy = new AddressHibernateProxy(realAddress, false);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(lazyProxy);

        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        assertThatJson(json).node("persistenceId").isEqualTo(1);
        assertThatJson(json).node("name").isEqualTo("John Doe");
        // LAZY proxy should NOT have its fields unwrapped
        assertThatJson(json).node("address").isObject();
        assertThatJson(json).node("address.street").isAbsent();
    }

    @Test
    public void serializeEntity_should_not_throw_LazyInitializationException_for_initialized_proxy() throws Exception {
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        // initialized=true: proxy delegates to real entity without LazyInitializationException
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress, true);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(proxyAddress);

        // should NOT throw LazyInitializationException
        assertThatNoException()
                .isThrownBy(() -> serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE));
    }

    @Test
    public void serializeEntity_should_serialize_cleanly_when_optional_child_is_null() throws Exception {
        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(null);

        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        assertThatJson(json).node("persistenceId").isEqualTo(1);
        assertThatJson(json).node("name").isEqualTo("John Doe");
    }

    @Test
    public void serializeEntity_should_generate_links_for_json_ignore_field_on_proxy_child() throws Exception {
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        realAddress.setDoorCode("1234A");
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(proxyAddress);

        String json = serializer.serializeEntity(person, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        // doorCode should NOT be a direct field (it's @JsonIgnore)
        assertThatJson(json).node("address.doorCode").isAbsent();
        // But address should have links for the ignored field
        assertThatJson(json).node("address.links").isArray().isNotEmpty();
    }

    @Test
    public void serializeEntity_should_serialize_collection_fields_on_proxy_entity() throws Exception {
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        realAddress.setFloors(java.util.Arrays.asList(0.0, 1.0, 4.0));
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        String json = serializer.serializeEntity(proxyAddress, PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        assertThatJson(json).node("street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("floors").isArray().hasSize(3);
    }

    @Test
    public void serializeEntities_should_include_all_fields_when_child_is_proxy() throws Exception {
        Address realAddress = createAddress(123L, "Rue Gustave Eiffel", 32f);
        AddressHibernateProxy proxyAddress = new AddressHibernateProxy(realAddress);

        PersonWithProxyAddress person = new PersonWithProxyAddress(1L, "John Doe");
        person.setAddress(proxyAddress);

        String json = serializer.serializeEntities(
                java.util.Collections.singletonList(person), PARAMETER_BUSINESSDATA_CLASS_URI_VALUE);

        assertThatJson(json).isArray().hasSize(1);
        assertThatJson(json).node("[0].address.street").isEqualTo("Rue Gustave Eiffel");
        assertThatJson(json).node("[0].address.number").isEqualTo(32.0);
    }

    private static Address createAddress(Long persistenceId, String street, Float number) {
        Address address = new Address();
        address.setPersistenceId(persistenceId);
        address.setPersistenceVersion(1L);
        address.setStreet(street);
        address.setNumber(number);
        return address;
    }

    private static Entity unwrapProxy(Entity entity) {
        if (entity instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) entity;
            return (Entity) proxy.getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
