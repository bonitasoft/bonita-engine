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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.bonitasoft.engine.api.BonitaStackTraceElementConverter;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.xml.XStreamDenyList;

public class XmlConverter {

    private static volatile XStream xstream;

    private static XStream getXStream() {
        if (xstream == null) {
            synchronized (XmlConverter.class) {
                if (xstream == null) {
                    xstream = createXStream();
                }
            }
        }
        return xstream;
    }

    private static XStream createXStream() {
        var xs = new XStream();
        xs.ignoreUnknownElements();
        xs.addPermission(AnyTypePermission.ANY);
        // Block known deserialization gadget chain libraries to prevent RCE.
        // A strict allowlist is not possible here because API responses can contain BDM types with arbitrary packages.
        xs.denyTypesByWildcard(XStreamDenyList.getDenyPatterns());
        xs.registerConverter(new BonitaStackTraceElementConverter(), XStream.PRIORITY_VERY_HIGH);
        return xs;
    }

    // Package-private for testing
    static void reset() {
        xstream = null;
    }

    public String toXML(final Object object) {
        final StringWriter stringWriter = new StringWriter();
        try (final ObjectOutputStream out = getXStream().createObjectOutputStream(stringWriter)) {
            out.writeObject(object);
        } catch (IOException e) {
            throw new BonitaRuntimeException("Unable to serialize object " + object, e);
        }
        return stringWriter.toString();
    }

    @SuppressWarnings("unchecked")
    public <T> T fromXML(final String object) {
        try (final StringReader xmlReader = new StringReader(object);
                final ObjectInputStream in = getXStream().createObjectInputStream(xmlReader)) {
            return (T) in.readObject();
        } catch (final ClassNotFoundException | IOException | RuntimeException e) {
            // Do not include the XML payload in the error message to prevent
            // information disclosure of attacker-controlled input
            throw new BonitaRuntimeException("Unable to deserialize object", e);
        }
    }
}
