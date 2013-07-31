/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package com.bonitasoft.engine.scheduler.impl;

import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.scheduler.impl.BonitaSchedulerFactory;
import org.quartz.SchedulerException;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Baptiste Mesta
 */
public class BonitaSchedulerFactoryExtended extends BonitaSchedulerFactory {

    public BonitaSchedulerFactoryExtended(final Properties props, final Properties additionalProperties) throws SchedulerException {
        super(merge(props, additionalProperties));
    }

    private static Properties merge(final Properties props, final Properties additionalProperties) {
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        final Properties properties = new Properties();
        for (final Entry<Object, Object> entry : props.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        for (final Entry<Object, Object> entry : additionalProperties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }

}
