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
package org.bonitasoft.console.common.server.filter;

import static org.bonitasoft.engine.mdc.MDCConstants.CORRELATION_REQUEST_ID;
import static org.bonitasoft.engine.mdc.MDCConstants.REQUEST_ID;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.classic.ClassicConstants;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.engine.mdc.AbstractMDC;
import org.bonitasoft.engine.mdc.MDCHelper;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.slf4j.MDC;

public class RequestIdFilter implements Filter {

    private String requestIdAttributeName = PropertiesFactory.getConsoleProperties().getRequestIdAttributeName();
    private String requestIdHeaderName = PropertiesFactory.getConsoleProperties().getRequestIdHeaderName();
    private String correlationIdAttributeName = PropertiesFactory.getConsoleProperties()
            .getCorrelationIdAttributeName();
    private String correlationIdHeaderName = PropertiesFactory.getConsoleProperties().getCorrelationIdHeaderName();

    /**
     * Check if the current request has a requestId and a correlationId.
     * If no requestId is found, tries and finds in header or generates a new requestId and attach it to the request.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // clean MDC from null request values inserted by MDCInsertingServletFilter...
        var nullable = Stream.of(ClassicConstants.REQUEST_QUERY_STRING, ClassicConstants.REQUEST_USER_AGENT_MDC_KEY,
                ClassicConstants.REQUEST_X_FORWARDED_FOR);
        nullable.filter(k -> MDC.get(k) == null).forEach(MDC::remove);
        // get request id already attached
        var attachedReqIdAtt = Optional.ofNullable((String) request.getAttribute(requestIdAttributeName));
        var requestId = attachedReqIdAtt.orElseGet(() -> {
            // look in header
            String headerRequestId = null;
            if (request instanceof HttpServletRequest httpRequest) {
                headerRequestId = httpRequest.getHeader(requestIdHeaderName);
            }
            var id = Optional.ofNullable(headerRequestId).orElseGet(() -> {
                // generate a new request id
                return Long.toHexString(System.nanoTime());
            });
            // attach it
            request.setAttribute(requestIdAttributeName, id);
            return id;
        });

        // find correlation id
        var attachedCorrelIdAtt = Optional.ofNullable((String) request.getAttribute(correlationIdAttributeName));
        var correlationId = attachedCorrelIdAtt.or(() -> {
            // look in header
            String headerCorrelId = null;
            if (request instanceof HttpServletRequest httpRequest) {
                headerCorrelId = httpRequest.getHeader(correlationIdHeaderName);
            }
            var id = Optional.ofNullable(headerCorrelId);
            // attach it
            id.ifPresent(i -> request.setAttribute(correlationIdAttributeName, i));
            return id;
        });

        Supplier<AbstractMDC> mdc = () -> new AbstractMDC(buildContextMap(requestId, correlationId)) {
        };

        MDCHelper.CheckedRunnable2<IOException, ServletException> call = () -> {
            chain.doFilter(request, response);
        };

        MDCHelper.tryWithMDC(mdc, call);

    }

    private static Map<String, String> buildContextMap(String requestId,
            Optional<String> correlationId) {
        Predicate<String> isBlank = StringUtil::isBlank;
        if (correlationId.filter(isBlank.negate()).isPresent()) {
            return Map.of(
                    REQUEST_ID, Objects.requireNonNull(requestId),
                    CORRELATION_REQUEST_ID, Objects.requireNonNull(correlationId.orElse(null)));
        } else {
            return Map.of(
                    REQUEST_ID, Objects.requireNonNull(requestId));
        }
    }

}
