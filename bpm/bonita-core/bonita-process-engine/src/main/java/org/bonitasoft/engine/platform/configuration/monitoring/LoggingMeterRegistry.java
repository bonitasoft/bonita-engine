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

import static io.micrometer.core.instrument.util.DoubleFormat.decimalOrNan;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.instrument.step.StepDistributionSummary;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepTimer;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.core.instrument.util.TimeUtils;
import io.micrometer.core.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspired by LoggingMeterRegistry by Jon Schneider {@link io.micrometer.core.instrument.logging.LoggingMeterRegistry}.
 */
public class LoggingMeterRegistry extends StepMeterRegistry {

    public static final Logger logger = LoggerFactory.getLogger(LoggingMeterRegistry.class);

    private final LoggingRegistryConfig config;

    public LoggingMeterRegistry() {
        this(LoggingRegistryConfig.DEFAULT, Clock.SYSTEM);
    }

    public LoggingMeterRegistry(LoggingRegistryConfig config, Clock clock) {
        this(config, clock, new NamedThreadFactory("metrics-logger"), null);
    }

    private LoggingMeterRegistry(LoggingRegistryConfig config, Clock clock, ThreadFactory threadFactory,
            @Nullable Function<Meter, String> meterIdPrinter) {
        super(config, clock);
        this.config = config;
        config().namingConvention(NamingConvention.dot);
        start(threadFactory);
    }

    private String tags(Meter meter) {
        List<Tag> conventionTags = getConventionTags(meter.getId());
        if (conventionTags.isEmpty()) {
            return "";
        }
        return conventionTags.stream()
                .map(t -> t.getKey() + "=" + t.getValue())
                .collect(joining(", ", " {", "}"));
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        if (config.enabled()) {
            logger.info("publishing metrics to logs every " + TimeUtils.format(config.step()));
        }
        super.start(threadFactory);
    }

    @Override
    protected Counter newCounter(Meter.Id id) {
        return new CumulativeAndStepCounter(id, clock, config.step().toMillis());
    }

    @Override
    protected void publish() {
        if (config.enabled()) {
            getMeters().stream()
                    .sorted((m1, m2) -> {
                        int typeComp = m1.getId().getType().compareTo(m2.getId().getType());
                        if (typeComp == 0) {
                            return m1.getId().getName().compareTo(m2.getId().getName());
                        }
                        return typeComp;
                    })
                    .forEach(m -> {
                        Printer print = new Printer(m);
                        logger.info(stream(m.measure().spliterator(), false)
                                .map(ms -> {
                                    String msLine = ms.getStatistic().getTagValueRepresentation() + "=";
                                    switch (ms.getStatistic()) {
                                        case TOTAL:
                                            return "value=" + print.value(ms.getValue());
                                        case MAX:
                                        case VALUE:
                                            return msLine + print.value(ms.getValue());
                                        case TOTAL_TIME:
                                        case DURATION:
                                            return msLine + print.time(ms.getValue());
                                        case COUNT:
                                            return "throughput=" + print.rate(ms.getValue());
                                        default:
                                            return msLine + decimalOrNan(ms.getValue());
                                    }
                                })
                                .collect(joining(", ", print.id() + " ", "")));

                    });
        }
    }

    @Override
    protected Timer newTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig,
            PauseDetector pauseDetector) {
        return new StepTimer(id, clock, distributionStatisticConfig, pauseDetector, getBaseTimeUnit(),
                this.config.step().toMillis(), false);
    }

    @Override
    protected DistributionSummary newDistributionSummary(Meter.Id id,
            DistributionStatisticConfig distributionStatisticConfig, double scale) {
        return new StepDistributionSummary(id, clock, distributionStatisticConfig, scale,
                config.step().toMillis(), false);
    }

    class Printer {

        private final Meter meter;

        Printer(Meter meter) {
            this.meter = meter;
        }

        String id() {
            return getConventionName(meter.getId()) + tags(meter);
        }

        String time(double time) {
            return TimeUtils
                    .format(Duration.ofNanos((long) TimeUtils.convert(time, getBaseTimeUnit(), TimeUnit.NANOSECONDS)));
        }

        String rate(double rate) {
            return humanReadableBaseUnit(rate / (double) config.step().getSeconds()) + "/s";
        }

        String value(double value) {
            return humanReadableBaseUnit(value);
        }

        String humanReadableBaseUnit(double value) {
            String baseUnit = meter.getId().getBaseUnit();
            return decimalOrNan(value) + (baseUnit != null ? " " + baseUnit : "");
        }
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
