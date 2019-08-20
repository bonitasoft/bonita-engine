/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.configuration;

import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Danila Mazour
 */
public class MeterRegistryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeterRegistryFactory.class);
    private List<MeterRegistry> meterRegistries;

    public MeterRegistry create() {
        if (meterRegistries == null || meterRegistries.isEmpty()) {
            LOGGER.debug("No meter registry configured, using SimpleMeterRegistry");
            return new SimpleMeterRegistry();
        } else if (meterRegistries.size() == 1) {
            LOGGER.debug("Using single meter registry : {}", meterRegistries.get(0).getClass().getName());
            return meterRegistries.get(0);
        } else {
            LOGGER.debug("Using composite meter registry : {}", meterRegistries.stream().map((m) -> m.getClass().getName()).collect(Collectors.joining(", ")));
            CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            meterRegistries.forEach(compositeMeterRegistry::add);
            return compositeMeterRegistry;
        }
    }

    public void setMeterRegistries(List<MeterRegistry> meterRegistries) {
        this.meterRegistries = meterRegistries;
    }

}
