/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthenticationAuditLoggerTest {

    private final AuthenticationAuditLogger auditLogger = new AuthenticationAuditLogger();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = (Logger) LoggerFactory.getLogger(AuthenticationAuditLogger.LOGGER_NAME);

    private ListAppender<ILoggingEvent> appender;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // logback-test.xml sets the root logger to WARN: pin the level so that the INFO events are emitted
        logger.setLevel(Level.INFO);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        request = new MockHttpServletRequest("POST", "/bonita/loginservice");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0");
        request.setQueryString("redirect=false&password=secret");
        request.addParameter("username", "walter.bates");
        request.addParameter("password", "secret");
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        // Restore inheritance from the root logger so the pinned level does not leak to other tests
        logger.setLevel(null);
    }

    @Test
    void loginSucceeded_should_log_one_json_event_with_all_fields() throws Exception {
        auditLogger.loginSucceeded(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.INFO);
        final JsonNode event = loggedEvent();
        assertThat(event.path("@timestamp").asText()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        assertThat(event.path("schema_version").asText()).isEqualTo("1");
        assertThat(event.path("event").path("action").asText()).isEqualTo("login");
        assertThat(event.path("event").path("outcome").asText()).isEqualTo("success");
        assertThat(event.path("event").has("reason")).isFalse();
        assertThat(event.path("user").path("name").asText()).isEqualTo("walter.bates");
        assertThat(event.path("auth").path("method").asText()).isEqualTo("standard");
        assertThat(event.path("auth").has("provider")).isFalse();
        assertThat(event.path("source").path("ip").asText()).isEqualTo("10.0.0.1");
        assertThat(event.path("user_agent").path("original").asText()).isEqualTo("Mozilla/5.0");
        assertThat(event.path("http").path("request").path("method").asText()).isEqualTo("POST");
        assertThat(event.path("url").path("path").asText()).isEqualTo("/bonita/loginservice");
        assertThat(event.path("session").path("id_hash").asText())
                .isEqualTo(AuthenticationAuditLogger.hash(42L))
                .hasSize(64)
                .isNotEqualTo("42");
    }

    @Test
    void loginSucceeded_should_ignore_the_end_user_context_headers_when_they_are_not_trusted() throws Exception {
        request.addHeader(AuthenticationAuditLogger.CLIENT_IP_HEADER, "185.159.97.97");
        request.addHeader(AuthenticationAuditLogger.CLIENT_USER_AGENT_HEADER, "Firefox/154.0");

        auditLogger.loginSucceeded(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("source").path("ip").asText()).isEqualTo("10.0.0.1");
        assertThat(event.path("user_agent").path("original").asText()).isEqualTo("Mozilla/5.0");
    }

    @Test
    void loginSucceeded_should_use_the_end_user_context_sent_by_ui_builder_when_it_is_trusted() throws Exception {
        request.addHeader(AuthenticationAuditLogger.CLIENT_IP_HEADER, "185.159.97.97");
        request.addHeader(AuthenticationAuditLogger.CLIENT_USER_AGENT_HEADER, "Firefox/154.0");

        new AuthenticationAuditLogger(true).loginSucceeded(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("source").path("ip").asText()).isEqualTo("185.159.97.97");
        assertThat(event.path("user_agent").path("original").asText()).isEqualTo("Firefox/154.0");
    }

    @Test
    void loginSucceeded_should_ignore_a_trusted_client_ip_header_that_is_not_an_ip_address() throws Exception {
        request.addHeader(AuthenticationAuditLogger.CLIENT_IP_HEADER, "185.159.97.97, 10.0.0.2");

        new AuthenticationAuditLogger(true).loginSucceeded(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        assertThat(loggedEvent().path("source").path("ip").asText()).isEqualTo("10.0.0.1");
    }

    @Test
    void formatTimestamp_should_always_write_milliseconds_in_utc() {
        assertThat(AuthenticationAuditLogger.formatTimestamp(Instant.parse("2026-10-16T09:12:03Z")))
                .isEqualTo("2026-10-16T09:12:03.000Z");
        assertThat(AuthenticationAuditLogger.formatTimestamp(Instant.parse("2026-10-16T09:12:03.512341Z")))
                .isEqualTo("2026-10-16T09:12:03.512Z");
        assertThat(AuthenticationAuditLogger.formatTimestamp(Instant.parse("2026-10-16T11:12:03.512+02:00")))
                .isEqualTo("2026-10-16T09:12:03.512Z");
    }

    @Test
    void isIpAddress_should_accept_literal_ipv4_and_ipv6_addresses_only() {
        assertThat(AuthenticationAuditLogger.isIpAddress("203.0.113.12")).isTrue();
        assertThat(AuthenticationAuditLogger.isIpAddress("::1")).isTrue();
        assertThat(AuthenticationAuditLogger.isIpAddress("2001:db8::7334")).isTrue();
        assertThat(AuthenticationAuditLogger.isIpAddress("::ffff:203.0.113.12")).isTrue();

        assertThat(AuthenticationAuditLogger.isIpAddress(null)).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("x")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("256.1.1.1")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("01.2.3.4")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("203.0.113.12, 10.0.0.2")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("bonita.example.com")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("1:2:3:4:5:6:7:8:9")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("fe80::1%eth0")).isFalse();
        // Values that InetAddress would resolve instead of parsing them
        assertThat(AuthenticationAuditLogger.isIpAddress(".:")).isFalse();
        assertThat(AuthenticationAuditLogger.isIpAddress("..::1")).isFalse();
    }

    @Test
    void loginSucceeded_should_truncate_the_values_coming_from_the_request() throws Exception {
        request.setRequestURI("/bonita/" + "p".repeat(3000));
        request.removeHeader("User-Agent");
        request.addHeader("User-Agent", "u".repeat(3000));

        auditLogger.loginSucceeded(request, "n".repeat(3000), "oidc", "o".repeat(3000), 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("user").path("name").asText())
                .hasSize(AuthenticationAuditLogger.MAX_USER_NAME_LENGTH).endsWith("...");
        assertThat(event.path("auth").path("provider").asText())
                .hasSize(AuthenticationAuditLogger.MAX_AUTH_PROVIDER_LENGTH).endsWith("...");
        assertThat(event.path("user_agent").path("original").asText())
                .hasSize(AuthenticationAuditLogger.MAX_USER_AGENT_LENGTH).endsWith("...");
        assertThat(event.path("url").path("path").asText())
                .hasSize(AuthenticationAuditLogger.MAX_URL_PATH_LENGTH).endsWith("...");
    }

    @Test
    void loginSucceeded_should_never_log_the_password_the_query_string_nor_the_path_parameters() throws Exception {
        request.setRequestURI("/bonita/loginservice;jsessionid=0123456789ABCDEF");

        auditLogger.loginSucceeded(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .doesNotContain("secret")
                .doesNotContain("redirect=false")
                .doesNotContain("0123456789ABCDEF");
        assertThat(loggedEvent().path("url").path("path").asText()).isEqualTo("/bonita/loginservice");
    }

    @Test
    void loginFailed_should_write_a_single_line_when_the_user_name_contains_line_breaks() throws Exception {
        auditLogger.loginFailed(request, "walter\nbates\r\n{\"forged\":true}",
                AuthenticationAuditLogger.METHOD_STANDARD, null);

        assertThat(appender.list.get(0).getFormattedMessage()).doesNotContain("\n").doesNotContain("\r");
        assertThat(loggedEvent().path("user").path("name").asText())
                .isEqualTo("walter\nbates\r\n{\"forged\":true}");
    }

    @Test
    void loginSucceeded_should_not_create_empty_objects_for_missing_values() throws Exception {
        final MockHttpServletRequest bareRequest = new MockHttpServletRequest();
        bareRequest.setRemoteAddr(null);
        bareRequest.setMethod(null);
        bareRequest.setRequestURI(null);

        auditLogger.loginSucceeded(bareRequest, null, null, 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.has("user")).isFalse();
        assertThat(event.has("auth")).isFalse();
        assertThat(event.has("source")).isFalse();
        assertThat(event.has("user_agent")).isFalse();
        assertThat(event.has("http")).isFalse();
        assertThat(event.has("url")).isFalse();
    }

    @Test
    void loginSucceeded_should_not_throw_when_the_event_cannot_be_built() {
        final HttpServletRequest brokenRequest = mock(HttpServletRequest.class);
        doThrow(new IllegalStateException("broken")).when(brokenRequest).getRemoteAddr();

        assertThatCode(() -> auditLogger.loginSucceeded(brokenRequest, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, 42L)).doesNotThrowAnyException();
        assertThat(appender.list).isEmpty();
    }

    @Test
    void loginSucceeded_should_log_nothing_when_the_audit_logger_is_off() {
        logger.setLevel(Level.OFF);

        auditLogger.loginSucceeded(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD, 42L);

        assertThat(appender.list).isEmpty();
    }

    @Test
    void loginFailed_should_log_a_failure_event_without_reason_nor_session() throws Exception {
        auditLogger.loginFailed(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD, null);

        final JsonNode event = loggedEvent();
        assertThat(event.path("event").path("action").asText()).isEqualTo("login");
        assertThat(event.path("event").path("outcome").asText()).isEqualTo("failure");
        assertThat(event.path("event").has("reason")).isFalse();
        assertThat(event.path("user").path("name").asText()).isEqualTo("walter.bates");
        assertThat(event.path("auth").path("method").asText()).isEqualTo("standard");
        assertThat(event.path("source").path("ip").asText()).isEqualTo("10.0.0.1");
        assertThat(event.path("url").path("path").asText()).isEqualTo("/bonita/loginservice");
        assertThat(event.has("session")).isFalse();
    }

    @Test
    void loginFailed_should_log_the_locked_out_reason() throws Exception {
        auditLogger.loginFailed(request, "walter.bates", AuthenticationAuditLogger.METHOD_STANDARD,
                AuthenticationAuditLogger.REASON_LOCKED_OUT);

        assertThat(loggedEvent().path("event").path("reason").asText()).isEqualTo("locked_out");
    }

    @Test
    void loggedOut_should_log_a_logout_event_with_the_login_method_and_session_hash() throws Exception {
        request.setRequestURI("/bonita/logoutservice");

        auditLogger.loggedOut(request, "walter.bates", "oidc", 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("event").path("action").asText()).isEqualTo("logout");
        assertThat(event.path("event").path("outcome").asText()).isEqualTo("success");
        assertThat(event.path("user").path("name").asText()).isEqualTo("walter.bates");
        assertThat(event.path("auth").path("method").asText()).isEqualTo("oidc");
        assertThat(event.path("url").path("path").asText()).isEqualTo("/bonita/logoutservice");
        assertThat(event.path("session").path("id_hash").asText()).isEqualTo(AuthenticationAuditLogger.hash(42L));
    }

    @Test
    void loginSucceeded_should_log_the_identity_provider_next_to_the_method() throws Exception {
        auditLogger.loginSucceeded(request, "walter.bates", "oidc", "google", 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("auth").path("method").asText()).isEqualTo("oidc");
        assertThat(event.path("auth").path("provider").asText()).isEqualTo("google");
        assertThat(event.path("session").path("id_hash").asText()).isEqualTo(AuthenticationAuditLogger.hash(42L));
    }

    @Test
    void loginFailed_should_log_the_identity_provider_of_the_attempt() throws Exception {
        auditLogger.loginFailed(request, "unknown.user", "oidc", "google", null);

        final JsonNode event = loggedEvent();
        assertThat(event.path("event").path("outcome").asText()).isEqualTo("failure");
        assertThat(event.path("event").has("reason")).isFalse();
        assertThat(event.path("auth").path("method").asText()).isEqualTo("oidc");
        assertThat(event.path("auth").path("provider").asText()).isEqualTo("google");
    }

    @Test
    void loggedOut_should_log_the_identity_provider_of_the_login() throws Exception {
        auditLogger.loggedOut(request, "walter.bates", "oidc", "google", 42L);

        final JsonNode event = loggedEvent();
        assertThat(event.path("event").path("action").asText()).isEqualTo("logout");
        assertThat(event.path("auth").path("method").asText()).isEqualTo("oidc");
        assertThat(event.path("auth").path("provider").asText()).isEqualTo("google");
    }

    private JsonNode loggedEvent() throws Exception {
        assertThat(appender.list).hasSize(1);
        return objectMapper.readTree(appender.list.get(0).getFormattedMessage());
    }
}
