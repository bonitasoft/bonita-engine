/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.persistence;

import java.io.Serializable;

import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;

public class XMLTypeDescriptor
        extends AbstractTypeDescriptor<Serializable> {

    private static PrimitiveByteArrayTypeDescriptor byteArrayTypeDescriptor = PrimitiveByteArrayTypeDescriptor.INSTANCE;

    public XMLTypeDescriptor() {
        super(Serializable.class, ImmutableMutabilityPlan.INSTANCE);
    }

    @Override
    public boolean areEqual(Serializable one, Serializable another) {
        if (one == another) {
            return true;
        }
        if (one == null || another == null) {
            return false;
        }
        return one.equals(another);
    }

    @Override
    public String toString(Serializable value) {
        return XStreamFactory.getXStream().toXML(value);
    }

    @Override
    public Serializable fromString(String string) {
        return ((Serializable) XStreamFactory.getXStream().fromXML((string)));
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <X> X unwrap(Serializable value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        return byteArrayTypeDescriptor.unwrap(javaSerialize(toString((value))), type, options);
    }

    private byte[] javaSerialize(String value) {
        return SerializationHelper.serialize(value);
    }

    private String javaDeserialize(byte[] value) {
        return ((String) SerializationHelper.deserialize(value));
    }

    @Override
    public <X> Serializable wrap(X value, WrapperOptions options) {
        byte[] bytes = byteArrayTypeDescriptor.wrap(value, options);
        if (bytes == null) {
            return null;
        }
        return fromString(javaDeserialize(bytes));
    }

}
