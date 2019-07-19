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
            LOGGER.info("No meter registry configured, using SimpleMeterRegistry");
            return new SimpleMeterRegistry();
        } else if (meterRegistries.size() == 1) {
            LOGGER.debug("Using single meter registry : {}", meterRegistries.get(0).getClass().getName());
            LOGGER.info("Using single meter registry : {}", meterRegistries.get(0).getClass().getName());
            return meterRegistries.get(0);
        } else {
            LOGGER.debug("Using composite meter registry : {}", meterRegistries.stream().map((m) -> m.getClass().getName()).collect(Collectors.joining(", ")));
            LOGGER.info("Using composite meter registry : {}", meterRegistries.stream().map((m) -> m.getClass().getName()).collect(Collectors.joining(", ")));
            CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
            meterRegistries.forEach(compositeMeterRegistry::add);
            return compositeMeterRegistry;
        }
    }

    public void setMeterRegistries(List<MeterRegistry> meterRegistries) {
        this.meterRegistries = meterRegistries;
    }

}
