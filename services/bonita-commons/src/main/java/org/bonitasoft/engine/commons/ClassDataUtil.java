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
package org.bonitasoft.engine.commons;

import java.io.IOException;
import java.io.InputStream;

import org.bonitasoft.engine.commons.io.IOUtil;

/**
 * @author Elias Ricken de Medeiros
 */
public class ClassDataUtil {

    public static byte[] getClassData(final Class<?> clazz) throws IOException {
        if (clazz == null) {
            final String message = "Class is null";
            throw new IOException(message);
        }
        final String resource = clazz.getName().replace('.', '/') + ".class";
        final InputStream inputStream = clazz.getClassLoader().getResourceAsStream(resource);
        byte[] data = null;
        try {
            if (inputStream == null) {
                throw new IOException("Impossible to get stream from class: " + clazz.getName() + ", className= " + resource);
            }
            data = IOUtil.getAllContentFrom(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }

}
