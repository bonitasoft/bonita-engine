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
package org.bonitasoft.engine.platform.configuration.monitoring;

import java.util.List;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
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
    private List<MeterBinder> meterBinders;

    public MeterRegistry create() {
        MeterRegistry registry = instantiate();
        meterBinders.forEach(b -> {
            LOGGER.debug("Register {} metrics on the registry", b.getClass().getName());
            b.bindTo(registry);
        });
        return registry;
    }

    private MeterRegistry instantiate() {
        if (meterRegistries == null || meterRegistries.isEmpty()) {
            LOGGER.info("No monitoring system registered, Metrics will not be published");
            return new SimpleMeterRegistry();
        } else if (meterRegistries.size() == 1) {
            LOGGER.info("Publishing monitoring metrics to : {}", meterRegistries.get(0).getClass().getName());
            return meterRegistries.get(0);
        } else {
            LOGGER.info("Publishing monitoring metrics to : {}",
                    meterRegistries.stream().map((m) -> m.getClass().getName()).collect(Collectors.joining(", ")));
            CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            meterRegistries.forEach(compositeMeterRegistry::add);
            return compositeMeterRegistry;
        }
    }

    public void setMeterRegistries(List<MeterRegistry> meterRegistries) {
        this.meterRegistries = meterRegistries;
    }

    public void setMeterBinders(List<MeterBinder> meterBinders) {
        this.meterBinders = meterBinders;
    }
}
