/**
 * Copyright (C) 2018 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.bonitasoft.engine.api.BonitaStackTraceElementConverter;
import org.bonitasoft.engine.exception.BonitaRuntimeException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

public class XmlConverter {

    private static final XStream XSTREAM;
    static {
        XSTREAM = new XStream();
        XSTREAM.ignoreUnknownElements();
        XStream.setupDefaultSecurity(XSTREAM);
        XSTREAM.addPermission(AnyTypePermission.ANY);
        XSTREAM.registerConverter(new BonitaStackTraceElementConverter(), XStream.PRIORITY_VERY_HIGH);
    }

    public String toXML(final Object object) {
        final StringWriter stringWriter = new StringWriter();
        try (final ObjectOutputStream out = XSTREAM.createObjectOutputStream(stringWriter)){
            out.writeObject(object);
        } catch (IOException e) {
            throw new BonitaRuntimeException("Unable to serialize object " + object, e);
        }
        return stringWriter.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> T fromXML(final String object) {
        try (final StringReader xmlReader = new StringReader(object);
                final ObjectInputStream in = XSTREAM.createObjectInputStream(xmlReader)) {
            return (T) in.readObject();
        } catch (final ClassNotFoundException | IOException | RuntimeException e) {
            throw new BonitaRuntimeException("Unable to deserialize object " + object, e);
        }
    }
}
