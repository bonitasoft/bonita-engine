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

import static java.util.Collections.emptyList;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.bonitasoft.engine.commons.Pair;

/**
 * Duplicated and extended from {@link io.micrometer.core.instrument.binder.tomcat.TomcatMetrics}
 * To be removed once original binder get properties only from jmx
 */
public class TomcatMetrics implements MeterBinder {

    private static final String JMX_DOMAIN_EMBEDDED = "Tomcat";
    private static final String JMX_DOMAIN_STANDALONE = "Catalina";
    private static final String OBJECT_NAME_SERVER_SUFFIX = ":type=Server";
    private static final String OBJECT_NAME_SERVER_EMBEDDED = JMX_DOMAIN_EMBEDDED + OBJECT_NAME_SERVER_SUFFIX;
    private static final String OBJECT_NAME_SERVER_STANDALONE = JMX_DOMAIN_STANDALONE + OBJECT_NAME_SERVER_SUFFIX;

    private final MBeanServer mBeanServer;
    private final Iterable<Tag> tags;

    private volatile String jmxDomain;

    public TomcatMetrics() {
        this(emptyList());
    }

    public TomcatMetrics(Iterable<Tag> tags) {
        this(tags, getMBeanServer());
    }

    public TomcatMetrics(Iterable<Tag> tags, MBeanServer mBeanServer) {
        this.tags = tags;
        this.mBeanServer = mBeanServer;
    }

    public static MBeanServer getMBeanServer() {
        List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if (!mBeanServers.isEmpty()) {
            return mBeanServers.get(0);
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        registerGlobalRequestMetrics(registry);
        registerServletMetrics(registry);
        registerCacheMetrics(registry);
        registerThreadPoolMetrics(registry);
        registerSessionMetrics(registry);
    }

    private void registerSessionMetrics(MeterRegistry registry) {
        registerMetricsEventually(
                new JMXQuery(getJmxDomain(), pair("type", "Manager"), pair("host", "*"), pair("context", "*")),
                (name, allTags) -> {

                    Gauge.builder("tomcat.sessions.active.max", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "maxActive")))
                            .tags(allTags)
                            .description("Maximum number of active sessions so far")
                            .baseUnit("sessions")
                            .register(registry);

                    Gauge.builder("tomcat.sessions.active.current", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "activeSessions")))
                            .tags(allTags)
                            .description("Number of active sessions at this moment")
                            .baseUnit("sessions")
                            .register(registry);

                    FunctionCounter
                            .builder("tomcat.sessions.created", mBeanServer,
                                    s -> safeDouble(() -> s.getAttribute(name, "sessionCounter")))
                            .tags(allTags)
                            .description("Total number of sessions created by this manager")
                            .baseUnit("sessions")
                            .register(registry);

                    FunctionCounter
                            .builder("tomcat.sessions.expired", mBeanServer,
                                    s -> safeDouble(() -> s.getAttribute(name, "expiredSessions")))
                            .tags(allTags)
                            .description("Number of sessions that expired ( doesn't include explicit invalidations )")
                            .baseUnit("sessions")
                            .register(registry);

                    FunctionCounter
                            .builder("tomcat.sessions.rejected", mBeanServer,
                                    s -> safeDouble(() -> s.getAttribute(name, "rejectedSessions")))
                            .tags(allTags)
                            .description("Number of sessions we rejected due to maxActive being reached")
                            .baseUnit("sessions")
                            .register(registry);
                });

    }

    private void registerThreadPoolMetrics(MeterRegistry registry) {
        registerMetricsEventually(new JMXQuery(getJmxDomain(), pair("type", "ThreadPool"), pair("name", "*")),
                (name, allTags) -> {
                    Gauge.builder("tomcat.threads.config.max", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "maxThreads")))
                            .tags(allTags)
                            .baseUnit(BaseUnits.THREADS)
                            .register(registry);

                    Gauge.builder("tomcat.threads.busy", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "currentThreadsBusy")))
                            .tags(allTags)
                            .baseUnit(BaseUnits.THREADS)
                            .register(registry);

                    Gauge.builder("tomcat.threads.current", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "currentThreadCount")))
                            .tags(allTags)
                            .baseUnit(BaseUnits.THREADS)
                            .register(registry);
                });
    }

    private void registerCacheMetrics(MeterRegistry registry) {
        registerMetricsEventually(new JMXQuery(getJmxDomain(), pair("type", "StringCache"), pair("name", "*")),
                (name, allTags) -> {
                    FunctionCounter.builder("tomcat.cache.access", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "accessCount")))
                            .tags(allTags)
                            .register(registry);

                    FunctionCounter.builder("tomcat.cache.hit", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "hitCount")))
                            .tags(allTags)
                            .register(registry);
                });
    }

    private void registerServletMetrics(MeterRegistry registry) {
        registerMetricsEventually(new JMXQuery(getJmxDomain(), pair("j2eeType", "Servlet"), pair("name", "*")),
                (name, allTags) -> {
                    FunctionCounter.builder("tomcat.servlet.error", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "errorCount")))
                            .tags(allTags)
                            .register(registry);

                    FunctionTimer.builder("tomcat.servlet.request", mBeanServer,
                            s -> safeLong(() -> s.getAttribute(name, "requestCount")),
                            s -> safeDouble(() -> s.getAttribute(name, "processingTime")), TimeUnit.MILLISECONDS)
                            .tags(allTags)
                            .register(registry);

                    TimeGauge.builder("tomcat.servlet.request.max", mBeanServer, TimeUnit.MILLISECONDS,
                            s -> safeDouble(() -> s.getAttribute(name, "maxTime")))
                            .tags(allTags)
                            .register(registry);
                });
    }

    private void registerGlobalRequestMetrics(MeterRegistry registry) {
        registerMetricsEventually(
                new JMXQuery(getJmxDomain(), pair("type", "GlobalRequestProcessor"), pair("name", "*")),
                (name, allTags) -> {
                    FunctionCounter.builder("tomcat.global.sent", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "bytesSent")))
                            .tags(allTags)
                            .baseUnit(BaseUnits.BYTES)
                            .register(registry);

                    FunctionCounter.builder("tomcat.global.received", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "bytesReceived")))
                            .tags(allTags)
                            .baseUnit(BaseUnits.BYTES)
                            .register(registry);

                    FunctionCounter.builder("tomcat.global.error", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "errorCount")))
                            .tags(allTags)
                            .register(registry);

                    FunctionTimer.builder("tomcat.global.request", mBeanServer,
                            s -> safeLong(() -> s.getAttribute(name, "requestCount")),
                            s -> safeDouble(() -> s.getAttribute(name, "processingTime")), TimeUnit.MILLISECONDS)
                            .tags(allTags)
                            .register(registry);

                    TimeGauge.builder("tomcat.global.request.max", mBeanServer, TimeUnit.MILLISECONDS,
                            s -> safeDouble(() -> s.getAttribute(name, "maxTime")))
                            .tags(allTags)
                            .register(registry);
                });
    }

    /**
     * If the MBean already exists, register metrics immediately. Otherwise register an MBean registration listener
     * with the MBeanServer and register metrics when/if the MBean becomes available.
     */
    private void registerMetricsEventually(JMXQuery jmxQuery, BiConsumer<ObjectName, Iterable<Tag>> perObject) {
        if (getJmxDomain() != null) {
            try {
                Set<ObjectName> objectNames = this.mBeanServer.queryNames(new ObjectName(jmxQuery.getQuery()), null);
                if (!objectNames.isEmpty()) {
                    // MBean is present, so we can register metrics now.
                    objectNames.forEach(objectName -> perObject.accept(objectName,
                            Tags.concat(tags, tagsFromQuery(jmxQuery, objectName))));
                    return;
                }
            } catch (MalformedObjectNameException e) {
                // should never happen
                throw new RuntimeException("Error registering Tomcat JMX based metrics", e);
            }
        }

        // MBean isn't yet registered, so we'll set up a notification to wait for them to be present and register
        // metrics later.
        NotificationListener notificationListener = new NotificationListener() {

            @Override
            public void handleNotification(Notification notification, Object handback) {
                MBeanServerNotification mBeanServerNotification = (MBeanServerNotification) notification;
                ObjectName objectName = mBeanServerNotification.getMBeanName();
                perObject.accept(objectName, Tags.concat(tags, tagsFromQuery(jmxQuery, objectName)));
                try {
                    mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
                } catch (InstanceNotFoundException | ListenerNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        NotificationFilter notificationFilter = (NotificationFilter) notification -> {
            if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
                return false;
            }

            // we can safely downcast now
            ObjectName objectName = ((MBeanServerNotification) notification).getMBeanName();
            return objectName.getDomain().equals(getJmxDomain()) && jmxQuery.getFixedValues()
                    .allMatch(p -> objectName.getKeyProperty(p.getKey()).equals(p.getValue()));
        };

        try {
            mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener,
                    notificationFilter, null);
        } catch (InstanceNotFoundException e) {
            // should never happen
            throw new RuntimeException("Error registering MBean listener", e);
        }
    }

    private String getJmxDomain() {
        if (this.jmxDomain == null) {
            if (hasObjectName(OBJECT_NAME_SERVER_EMBEDDED)) {
                this.jmxDomain = JMX_DOMAIN_EMBEDDED;
            } else if (hasObjectName(OBJECT_NAME_SERVER_STANDALONE)) {
                this.jmxDomain = JMX_DOMAIN_STANDALONE;
            }
        }
        return this.jmxDomain;
    }

    /**
     * customize jmx domain name of not Catalina or Tomcat
     *
     * @param jmxDomain
     */
    public void setJmxDomain(String jmxDomain) {
        this.jmxDomain = jmxDomain;
    }

    private boolean hasObjectName(String name) {
        try {
            return this.mBeanServer.queryNames(new ObjectName(name), null).size() == 1;
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        }
    }

    private double safeDouble(Callable<Object> callable) {
        try {
            return Double.parseDouble(callable.call().toString());
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private long safeLong(Callable<Object> callable) {
        try {
            return Long.parseLong(callable.call().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private Iterable<Tag> tagsFromQuery(JMXQuery jmxQuery, ObjectName object) {
        return jmxQuery.getWildCards().stream()
                .map(property -> pair(property, object.getKeyProperty(property)))
                .filter(p -> p.getValue() != null)
                .map(p -> Tag.of(p.getKey(), p.getValue().replaceAll("\"", "")))
                .collect(Collectors.toList());
    }

    private static class JMXQuery {

        private String domain;
        private List<Pair<String, String>> values;

        JMXQuery(String domain, Pair<String, String>... values) {
            this.domain = domain;
            this.values = Arrays.asList(values);
        }

        String getQuery() {
            return domain + ":"
                    + values.stream().map(p -> p.getKey() + "=" + p.getValue()).collect(Collectors.joining(","));
        }

        List<String> getWildCards() {
            return values.stream().filter(p -> p.getValue().equals("*")).map(Pair::getKey).collect(Collectors.toList());
        }

        Stream<Pair<String, String>> getFixedValues() {
            return values.stream().filter(p -> !p.getValue().equals("*"));
        }

    }
}
