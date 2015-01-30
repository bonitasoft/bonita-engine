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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public final class SABlobDataInstanceImpl extends SADataInstanceImpl {

    private static final long serialVersionUID = 2420374967139131312L;

    private byte[] value;

    public SABlobDataInstanceImpl() {
        super();
    }

    public SABlobDataInstanceImpl(final SDataInstance sDataInstance) {
        super(sDataInstance);
        setValue(sDataInstance.getValue());
    }

    @Override
    public String getDiscriminator() {
        return SABlobDataInstanceImpl.class.getSimpleName();
    }

    @Override
    public Serializable getValue() {
        return revert(value);
    }

    @Override
    public void setValue(final Serializable value) {
        this.value = convert(value);
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDataInstance.class;
    }

    private byte[] convert(final Serializable value) {
        ObjectOutputStream oos = null;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            return baos.toByteArray();
        } catch (final IOException ioe) {
            throw new SBonitaRuntimeException(ioe);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (final IOException ioe) {
                throw new SBonitaRuntimeException(ioe);
            }
        }
    }

    private Serializable revert(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais) {

                @Override
                protected Class<?> resolveClass(final ObjectStreamClass desc) throws ClassNotFoundException {
                    final String className = desc.getName();
                    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    return Class.forName(className, true, classLoader);
                }
            };
            return (Serializable) ois.readObject();
        } catch (final IOException ioe) {
            throw new SBonitaRuntimeException(ioe);
        } catch (final ClassNotFoundException cnfe) {
            throw new SBonitaRuntimeException(cnfe);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (final IOException ioe) {
                    throw new SBonitaRuntimeException(ioe);
                }
            }
        }
    }

}
