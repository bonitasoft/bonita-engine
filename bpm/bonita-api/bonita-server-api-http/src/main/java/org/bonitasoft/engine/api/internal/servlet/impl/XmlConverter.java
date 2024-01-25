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
package org.bonitasoft.engine.api.internal.servlet.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.bonitasoft.engine.exception.BonitaRuntimeException;

// TODO duplicated with the implementation in org.bonitasoft.engine.api.impl
// The only difference is the settings of Xstream
// TODO xstream instance should also ignored unknowns elements as the client to avoid issue in the future (fill a task)
public class XmlConverter {

    private static final XStream XSTREAM;
    static {
        XSTREAM = new XStream();
        XStream.setupDefaultSecurity(XSTREAM);
        XSTREAM.addPermission(AnyTypePermission.ANY);
        // ignore fields suppressedExceptions causing exceptions in some cases
        XSTREAM.omitField(Throwable.class, "suppressedExceptions");
    }

    public String toXML(final Object object) {
        final StringWriter stringWriter = new StringWriter();
        try (final ObjectOutputStream out = XSTREAM.createObjectOutputStream(stringWriter)) {
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
