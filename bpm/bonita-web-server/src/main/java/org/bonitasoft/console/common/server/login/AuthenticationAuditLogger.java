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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.filter.PathSanitizer;
import org.bonitasoft.engine.audit.AuditCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes one JSON line per authentication event (login success, login failure, logout) to the audit log, so that it
 * can be routed to a SIEM. The events go to the logger of the {@link AuditCategory#AUTHENTICATION} category
 * ({@link #LOGGER_NAME}), a child of {@value AuditCategory#PARENT_LOGGER_NAME}: the logging configuration
 * routes the parent logger, and can switch off the whole audit log or only the authentication events by setting
 * their logger to OFF. The loggers are on by default (INFO).
 * <p>
 * The event never contains the password, the request body, the query string or the path parameters (such as a
 * jsessionid). The values coming from the request are truncated, so that a client cannot inflate the audit log.
 */
public class AuthenticationAuditLogger {

    /**
     * The audit log category of the events, which gives their {@code event.category} and their logger.
     */
    private static final AuditCategory CATEGORY = AuditCategory.AUTHENTICATION;

    public static final String LOGGER_NAME = CATEGORY.getLoggerName();

    public static final String METHOD_STANDARD = "standard";

    public static final String METHOD_PLATFORM = "platform";

    static final String SCHEMA_VERSION = "1";

    /**
     * Headers set by the UI Builder backend on its calls to Bonita (EVOL-174), carrying the end user's context. They
     * are only used when trusted, see {@link #AuthenticationAuditLogger(boolean)}.
     */
    static final String CLIENT_IP_HEADER = "X-Bonita-Client-Ip";
    static final String CLIENT_USER_AGENT_HEADER = "X-Bonita-Client-User-Agent";

    static final String ACTION_LOGIN = "login";
    static final String ACTION_LOGOUT = "logout";
    static final String OUTCOME_SUCCESS = "success";
    static final String OUTCOME_FAILURE = "failure";
    static final String REASON_LOCKED_OUT = "locked_out";

    static final int MAX_USER_NAME_LENGTH = 256;
    static final int MAX_AUTH_PROVIDER_LENGTH = 256;
    static final int MAX_USER_AGENT_LENGTH = 512;
    static final int MAX_URL_PATH_LENGTH = 1024;

    private static final Pattern IPV4_ADDRESS = Pattern
            .compile("((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)");

    // InetAddress only parses the value as a literal when it starts with a hex digit or ':', it resolves it otherwise
    private static final Pattern IPV6_ADDRESS_CHARACTERS = Pattern.compile("[0-9a-fA-F:][0-9a-fA-F:.]*");

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationAuditLogger.class);

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger(LOGGER_NAME);

    // Shared by all instances: a LoginManager, and so an AuthenticationAuditLogger, is created for each login request
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Always milliseconds: Instant.toString() writes 0, 3, 6 or 9 fraction digits depending on the clock
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private final boolean trustClientHeaders;

    /**
     * Creates an audit logger that ignores the end-user context headers sent by the UI Builder backend.
     */
    public AuthenticationAuditLogger() {
        this(false);
    }

    /**
     * @param trustClientHeaders whether the end-user IP and User-Agent sent by the UI Builder backend are used
     *        (X-Bonita-Client-Ip and X-Bonita-Client-User-Agent headers). Only enable it when browsers cannot reach
     *        Bonita without going through the UI Builder proxy, which removes these headers from their requests.
     */
    public AuthenticationAuditLogger(final boolean trustClientHeaders) {
        this.trustClientHeaders = trustClientHeaders;
    }

    /**
     * Logs a successful login, without identity provider.
     *
     * @see #loginSucceeded(HttpServletRequest, String, String, String, long)
     */
    public void loginSucceeded(final HttpServletRequest request, final String userName, final String authMethod,
            final long sessionId) {
        loginSucceeded(request, userName, authMethod, null, sessionId);
    }

    /**
     * Logs a successful login.
     *
     * @param request the HTTP request that performed the login
     * @param userName the name of the logged-in user
     * @param authMethod the authentication method (e.g. {@link #METHOD_STANDARD})
     * @param authProvider the identity provider the user logged in with, or null
     * @param sessionId the id of the created session, hashed in the event
     */
    public void loginSucceeded(final HttpServletRequest request, final String userName, final String authMethod,
            final String authProvider, final long sessionId) {
        log(request, ACTION_LOGIN, OUTCOME_SUCCESS, null, userName, authMethod, authProvider, sessionId);
    }

    /**
     * Logs a failed login attempt, without identity provider.
     *
     * @see #loginFailed(HttpServletRequest, String, String, String, String)
     */
    public void loginFailed(final HttpServletRequest request, final String userName, final String authMethod,
            final String reason) {
        loginFailed(request, userName, authMethod, null, reason);
    }

    /**
     * Logs a failed login attempt.
     *
     * @param request the HTTP request of the login attempt
     * @param userName the name that was attempted
     * @param authMethod the authentication method (e.g. {@link #METHOD_STANDARD})
     * @param authProvider the identity provider of the login attempt, or null
     * @param reason the reason of the failure (only {@link #REASON_LOCKED_OUT} is reported), or null
     */
    public void loginFailed(final HttpServletRequest request, final String userName, final String authMethod,
            final String authProvider, final String reason) {
        log(request, ACTION_LOGIN, OUTCOME_FAILURE, reason, userName, authMethod, authProvider, null);
    }

    /**
     * Logs a logout, without identity provider.
     *
     * @see #loggedOut(HttpServletRequest, String, String, String, long)
     */
    public void loggedOut(final HttpServletRequest request, final String userName, final String authMethod,
            final long sessionId) {
        loggedOut(request, userName, authMethod, null, sessionId);
    }

    /**
     * Logs a logout.
     *
     * @param request the HTTP request that performed the logout
     * @param userName the name of the logged-out user
     * @param authMethod the authentication method of the login
     * @param authProvider the identity provider of the login, or null
     * @param sessionId the id of the closed session, hashed in the event to pair it with the login
     */
    public void loggedOut(final HttpServletRequest request, final String userName, final String authMethod,
            final String authProvider, final long sessionId) {
        log(request, ACTION_LOGOUT, OUTCOME_SUCCESS, null, userName, authMethod, authProvider, sessionId);
    }

    void log(final HttpServletRequest request, final String action, final String outcome, final String reason,
            final String userName, final String authMethod, final String authProvider, final Long sessionId) {
        if (!AUDIT_LOGGER.isInfoEnabled()) {
            return;
        }
        try {
            AUDIT_LOGGER.info(OBJECT_MAPPER.writeValueAsString(
                    buildEvent(request, action, outcome, reason, userName, authMethod, authProvider, sessionId)));
        } catch (final Exception e) {
            // Never break an authentication because of the audit log
            LOGGER.warn("Unable to write the authentication audit event: {}", e.getMessage());
            LOGGER.debug("Authentication audit event failure", e);
        }
    }

    ObjectNode buildEvent(final HttpServletRequest request, final String action, final String outcome,
            final String reason, final String userName, final String authMethod, final String authProvider,
            final Long sessionId) {
        final ObjectNode event = OBJECT_MAPPER.createObjectNode();
        event.put("@timestamp", formatTimestamp(Instant.now()));
        event.put("schema_version", SCHEMA_VERSION);
        putIfNotEmpty(event, CATEGORY.getValue(), "event", "category");
        putIfNotEmpty(event, action, "event", "action");
        putIfNotEmpty(event, outcome, "event", "outcome");
        putIfNotEmpty(event, reason, "event", "reason");
        putIfNotEmpty(event, StringUtils.abbreviate(userName, MAX_USER_NAME_LENGTH), "user", "name");
        putIfNotEmpty(event, authMethod, "auth", "method");
        putIfNotEmpty(event, StringUtils.abbreviate(authProvider, MAX_AUTH_PROVIDER_LENGTH), "auth", "provider");
        putIfNotEmpty(event, getClientIp(request), "source", "ip");
        putIfNotEmpty(event, StringUtils.abbreviate(getClientUserAgent(request), MAX_USER_AGENT_LENGTH),
                "user_agent", "original");
        putIfNotEmpty(event, request.getMethod(), "http", "request", "method");
        // Request path only: the query string and the path parameters (e.g. jsessionid) may carry sensitive data
        putIfNotEmpty(event,
                StringUtils.abbreviate(PathSanitizer.stripPathParameters(request.getRequestURI()), MAX_URL_PATH_LENGTH),
                "url", "path");
        if (sessionId != null) {
            putIfNotEmpty(event, hash(sessionId), "session", "id_hash");
        }
        putIfNotEmpty(event, HostName.VALUE, "host", "name");
        return event;
    }

    static String formatTimestamp(final Instant instant) {
        return TIMESTAMP_FORMAT.format(instant);
    }

    String getClientIp(final HttpServletRequest request) {
        if (trustClientHeaders) {
            final String clientIp = StringUtils.trim(request.getHeader(CLIENT_IP_HEADER));
            if (isIpAddress(clientIp)) {
                return clientIp;
            }
        }
        return request.getRemoteAddr();
    }

    String getClientUserAgent(final HttpServletRequest request) {
        if (trustClientHeaders) {
            final String clientUserAgent = request.getHeader(CLIENT_USER_AGENT_HEADER);
            if (StringUtils.isNotBlank(clientUserAgent)) {
                return clientUserAgent;
            }
        }
        return request.getHeader("User-Agent");
    }

    /**
     * Checks that the value is a literal IPv4 or IPv6 address, without any DNS lookup.
     */
    static boolean isIpAddress(final String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        if (IPV4_ADDRESS.matcher(value).matches()) {
            return true;
        }
        // A value containing ':' is parsed as an IPv6 literal (possibly embedding an IPv4), never resolved
        if (value.indexOf(':') >= 0 && IPV6_ADDRESS_CHARACTERS.matcher(value).matches()) {
            try {
                InetAddress.getByName(value);
                return true;
            } catch (final UnknownHostException e) {
                return false;
            }
        }
        return false;
    }

    static String hash(final long sessionId) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of()
                    .formatHex(digest.digest(Long.toString(sessionId).getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            // Cannot happen: every Java platform must support SHA-256
            throw new IllegalStateException(e);
        }
    }

    /**
     * Puts the value at the given path (parent objects first, field name last), creating the parent objects only
     * when there is a value, so that the event carries no empty objects.
     */
    private static void putIfNotEmpty(final ObjectNode root, final String value, final String... path) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        ObjectNode node = root;
        for (int i = 0; i < path.length - 1; i++) {
            node = node.has(path[i]) ? (ObjectNode) node.get(path[i]) : node.putObject(path[i]);
        }
        node.put(path[path.length - 1], value);
    }

    /**
     * Resolved when the first event is written rather than when the class is loaded (first login), since the lookup
     * may block on DNS, even when the audit log is switched off.
     */
    private static final class HostName {

        private static final String VALUE = resolveHostName();
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final Exception e) {
            LOGGER.debug("Unable to resolve the host name for the authentication audit log", e);
            return null;
        }
    }
}
