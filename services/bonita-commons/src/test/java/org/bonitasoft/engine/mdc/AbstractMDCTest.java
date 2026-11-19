/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author Vincent Hemery
 */
public class AbstractMDCTest {

    private static Logger log;
    private static ByteArrayOutputStream logStream;

    @BeforeClass
    public static void configureLogger() {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
        logEncoder.setContext(logCtx);
        logEncoder.setPattern("%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level – %msg {%X}%n");
        logEncoder.start();

        OutputStreamAppender<ILoggingEvent> logStreamAppender = new OutputStreamAppender<>();
        logStreamAppender.setContext(logCtx);
        logStreamAppender.setName("console");
        logStreamAppender.setEncoder(logEncoder);
        logStreamAppender.setImmediateFlush(true);
        logStream = new ByteArrayOutputStream();
        logStreamAppender.setOutputStream(logStream);
        logStreamAppender.start();

        log = logCtx.getLogger(AbstractMDCTest.class);
        log.addAppender(logStreamAppender);
    }

    @Test
    public void nestedMdc_should_temporarilyOverrideContext() {
        try (var firstMdc = new AbstractMDC(Map.of("key", "value1")) {
        }) {
            try (var secondMdc = new AbstractMDC(Map.of("key", "value2")) {
            }) {
                assertThat(MDC.get("key")).isEqualTo("value2");
            }
            assertThat(MDC.get("key")).isEqualTo("value1");
        }
    }

    @Test
    public void mdcClose_should_clearContext() {
        // given
        Map<String, String> ctxMap = Map.of("key", "value1", "other", "value2");
        for (int i = 0; i < 2; i++) {
            // when
            try (var mdc = new AbstractMDC(ctxMap) {
            }) {
                // then
                assertThat(MDC.get("key")).isEqualTo("value1");
                assertThat(MDC.get("other")).isEqualTo("value2");
                // when
            }
            // then
            assertThat(MDC.get("key")).isNull();
            assertThat(MDC.get("other")).isNull();
            // when
            logStream.reset();
            log.info("This is an empty context test");
            // then
            var str = logStream.toString();
            Assertions.assertThat(str).contains("This is an empty context test {}");
        }
    }

    @Test
    public void mdc_should_log() {
        // given
        Map<String, String> ctxMap = Map.of(MDCConstants.USER_ID, "2", MDCConstants.SUBSTITUTE_USER_ID, "1");
        try (var mdc = new AbstractMDC(ctxMap) {
        }) {
            // when
            logStream.reset();
            log.info("This is a test");
            // then
            var str = logStream.toString();
            Assertions.assertThat(str).contains("This is a test {");
            ctxMap.forEach((k, v) -> Assertions.assertThat(str).contains(k + "=" + v));
        }
    }

}
