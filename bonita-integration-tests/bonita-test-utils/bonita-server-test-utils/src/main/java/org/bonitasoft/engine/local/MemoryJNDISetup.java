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
package org.bonitasoft.engine.local;

import java.util.Map;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

public class MemoryJNDISetup {

    private final JndiTemplate jndiTemplate;

    private final Map<String, Object> jndiMapping;

    private final Logger logger = LoggerFactory.getLogger(MemoryJNDISetup.class.getSimpleName());

    public MemoryJNDISetup(final JndiTemplate jndiTemplate, final Map<String, Object> jndiMapping) {
        super();
        this.jndiTemplate = jndiTemplate;
        this.jndiMapping = jndiMapping;
    }

    public void init() throws NamingException {
        for (final Map.Entry<String, Object> addToJndi : jndiMapping.entrySet()) {
            logger.info("Binding " + addToJndi.getKey() + " @ " + addToJndi.getValue());
            jndiTemplate.bind(addToJndi.getKey(), addToJndi.getValue());
        }
    }

    public void clean() throws NamingException {
        for (final Map.Entry<String, Object> removeFromJndi : jndiMapping.entrySet()) {
            logger.info("Unbinding " + removeFromJndi.getKey());
            jndiTemplate.unbind(removeFromJndi.getKey());
        }
    }

}
