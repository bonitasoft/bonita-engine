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
package org.bonitasoft.engine.service.impl;

import java.util.Properties;

import org.springframework.core.env.PropertySource;

/**
 * @author Charles Souillard
 */
public class CustomPropertySource extends PropertySource<String> {

    private final Properties properties;

    public CustomPropertySource(final String name, final Properties properties) {
        super(name);
        this.properties = properties;
        //System.err.println("----- CustomPropertySource(" + name + ") Thread: " + Thread.currentThread().getId() + "-----");
        //Thread.dumpStack();
        //System.err.println("Loading properties: " + properties);
        //System.err.println("----- END CustomPropertySource(" + name + ") Thread: " + Thread.currentThread().getId() + "-----");
    }

    @Override
    public Object getProperty(String key) {
        final Object value = properties.get(key);
        System.err.println("--- (" + name + " --- Retrieving " + key + "=" + value);
        return value;
    }
}
