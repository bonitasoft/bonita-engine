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
package org.bonitasoft.engine.service.impl;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author Baptiste Mesta
 * 
 */
public class MapToPropertiesFactoryBean extends AbstractFactoryBean<Properties> {

    private Map<String, String> map;

    @Override
    public Class<?> getObjectType() {
        return Properties.class;
    }

    @Override
    protected Properties createInstance() throws Exception {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    public void setMap(final Map<String, String> map) {
        this.map = map;
    }
}
