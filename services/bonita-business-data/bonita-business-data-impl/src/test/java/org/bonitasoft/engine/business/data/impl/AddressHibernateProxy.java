/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import com.company.model.Address;
import org.bonitasoft.engine.bdm.Entity;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * A fake HibernateProxy that wraps an Address entity.
 * <p>
 * This mimics Hibernate's proxy behavior where the proxy class itself has null/empty fields,
 * but the real data is accessible via {@link LazyInitializer#getImplementation()}.
 * <p>
 * Used to reproduce the bug where HibernateProxy entities are serialized as empty objects
 * because {@link org.bonitasoft.engine.business.data.impl.jackson.EntityBeanSerializerModifier#changeProperties}
 * returns an empty property list for HibernateProxy types.
 */
public class AddressHibernateProxy implements HibernateProxy, Entity {

    private final Address realAddress;
    private final boolean initialized;
    private final LazyInitializer lazyInitializer;

    public AddressHibernateProxy(Address realAddress) {
        this(realAddress, true);
    }

    public AddressHibernateProxy(Address realAddress, boolean initialized) {
        this.realAddress = realAddress;
        this.initialized = initialized;
        this.lazyInitializer = createMockLazyInitializer(realAddress, initialized);
    }

    private static LazyInitializer createMockLazyInitializer(Address realAddress, boolean initialized) {
        LazyInitializer mockInitializer = mock(LazyInitializer.class);
        doReturn(Address.class).when(mockInitializer).getPersistentClass();
        doReturn(realAddress).when(mockInitializer).getImplementation();
        doReturn(!initialized).when(mockInitializer).isUninitialized();
        return mockInitializer;
    }

    @Override
    public Object writeReplace() {
        return null;
    }

    @Override
    public LazyInitializer getHibernateLazyInitializer() {
        return lazyInitializer;
    }

    @Override
    public Long getPersistenceId() {
        return realAddress != null ? realAddress.getPersistenceId() : null;
    }

    @Override
    public Long getPersistenceVersion() {
        return realAddress != null ? realAddress.getPersistenceVersion() : null;
    }

    public String getStreet() {
        throwIfUninitialized("street");
        return realAddress != null ? realAddress.getStreet() : null;
    }

    public Float getNumber() {
        throwIfUninitialized("number");
        return realAddress != null ? realAddress.getNumber() : null;
    }

    public List<Double> getFloors() {
        throwIfUninitialized("floors");
        return realAddress != null ? realAddress.getFloors() : null;
    }

    public String getDoorCode() {
        throwIfUninitialized("doorCode");
        return realAddress != null ? realAddress.getDoorCode() : null;
    }

    private void throwIfUninitialized(String propertyName) {
        if (!initialized) {
            throw new org.hibernate.LazyInitializationException(
                    "could not initialize proxy - no Session: " + propertyName);
        }
    }
}
