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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * This class is used to filter malicious payload (e.g. XSS injection) by
 * neutralizing the injected code.
 *
 * @author Vincent Hemery
 */
public class SanitizerFilter extends ExcludingPatternFilter {

    /**
     * Sanitizer to apply to values.
     * Do not let TABLES and LINKS which can be mis-leading as phishing.
     */
    private static final PolicyFactory sanitizer = Sanitizers.BLOCKS.and(Sanitizers.FORMATTING).and(Sanitizers.STYLES)
            .and(Sanitizers.IMAGES);

    /** The HTTP methods concerned by this filter. */
    private static final String[] CONCERNED_METHODS = { "POST", "PUT", "PATCH" };

    /** Json object mapper */
    private ObjectMapper mapper = new ObjectMapper();

    private boolean isEnabled = PropertiesFactory.getSecurityProperties().isSanitizerProtectionEnabled();

    private List<String> attributesExcluded = PropertiesFactory.getSecurityProperties()
            .getAttributeExcludedFromSanitizerProtection();

    @Override
    public void destroy() {
    }

    @Override
    public String getDefaultExcludedPages() {
        // excludes nothing for now
        return "";
    }

    @Override
    public void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final HttpServletRequest req = (HttpServletRequest) request;
        var method = req.getMethod();
        if (!isSanitizerEnabled() || method == null
                || Stream.of(CONCERNED_METHODS).noneMatch(method::equalsIgnoreCase)) {
            // no body to sanitize, just skip this filter
            chain.doFilter(req, response);
            return;
        }
        // get body of request as Json
        var body = getJsonBody(req);
        // sanitize body
        final var sanitized = sanitize(body);
        if (sanitized.isPresent()) {
            // serialize the sanitized json node
            byte[] saneBodyBytes = mapper.writeValueAsBytes(sanitized.get());

            // wrap request with sanitized body for input stream
            sanitized.get();
            final var wrapper = new HttpServletRequestWrapper(req) {

                private ServletInputStream inputStream = null;

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    if (inputStream == null) {
                        final ByteArrayInputStream is = new ByteArrayInputStream(saneBodyBytes);
                        inputStream = new ServletInputStream() {

                            @Override
                            public int read() throws IOException {
                                return is.read();
                            }

                            @Override
                            public boolean isFinished() {
                                return is.available() == 0;
                            }

                            @Override
                            public boolean isReady() {
                                return !isFinished();
                            }

                            @Override
                            public void setReadListener(ReadListener readListener) {
                                throw new UnsupportedOperationException("Unimplemented method 'setReadListener'");
                            }
                        };
                    }
                    return inputStream;
                }

                @Override
                public int getContentLength() {
                    return saneBodyBytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return saneBodyBytes.length;
                }

            };
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(req, response);
        }
    }

    /**
     * Sanitize the given JsonNode.
     *
     * @param node Json node to sanitize
     * @return the sanitized Json node if it has effectively changed
     */
    protected Optional<JsonNode> sanitize(JsonNode node) {
        if (node == null) {
            return Optional.empty();
        } else if (node.isObject()) {
            AtomicBoolean changed = new AtomicBoolean(false);
            ObjectNode object = (ObjectNode) node;
            List<Runnable> operationsToPerformAfterIteration = new ArrayList<>();
            object.fields().forEachRemaining(entry -> {
                var key = entry.getKey();
                var newKey = sanitizeValueAndPerformAction(key, s -> {
                    // can't remove key while iterating, or we get a ConcurrentModificationException
                    operationsToPerformAfterIteration.add(() -> {
                        object.remove(key);
                        object.set(s, entry.getValue());
                    });
                    changed.set(true);
                }).orElse(key);

                if (getAttributesExcluded() == null || !getAttributesExcluded().contains(key)) {
                    var value = entry.getValue();
                    sanitize(value).ifPresent(v -> {
                        object.set(newKey, v);
                        changed.set(true);
                    });
                }
            });
            operationsToPerformAfterIteration.forEach(Runnable::run);
            return changed.get() ? Optional.of(object) : Optional.empty();
        } else if (node.isArray()) {
            AtomicBoolean changed = new AtomicBoolean(false);
            ArrayNode array = (ArrayNode) node;
            for (int i = 0; i < array.size(); i++) {
                var value = array.get(i);
                final int index = i;
                sanitize(value).ifPresent(v -> {
                    array.set(index, v);
                    changed.set(true);
                });
            }
            return changed.get() ? Optional.of(array) : Optional.empty();
        } else if (node.isValueNode()) {
            if (node.isBoolean() || node.isNumber() || node.isPojo() || StringUtils.isEmpty(node.textValue())) {
                // that's safe
                return Optional.empty();
            }
            var changedValue = sanitizeValueAndPerformAction(node.textValue(), v -> {
            });
            return changedValue.map(TextNode::new);
        }
        return Optional.empty();
    }

    private JsonNode getJsonBody(HttpServletRequest request) throws ServletException {
        if (request.getContentType() != null && request.getContentType().toLowerCase().startsWith("application/json")) {
            try (ServletInputStream inputStream = request.getInputStream()) {
                if (inputStream == null) {
                    return null;
                }
                String characterEncoding = Optional.ofNullable(request.getCharacterEncoding()).orElse("UTF-8");
                var stringBody = IOUtils.toString(inputStream, characterEncoding);
                if (!stringBody.isBlank()) {
                    return mapper.readTree(stringBody);
                }
            } catch (IOException e) {
                throw new ServletException(e);
            }
        }
        return null;
    }

    /**
     * Sanitize the value and perform the action only when value has changed
     *
     * @param value String value to sanitize
     * @param action action to perform when value has changed
     * @return the sanitized value if it has changed
     */
    private Optional<String> sanitizeValueAndPerformAction(String value, Consumer<String> action) {
        /*
         * Sanitize the value.
         * It's not just about applying the sanitizer...
         * We want the value to contain unescaped characters, but no script.
         * To avoid values with multiple escaping, we unescape untill the value does not
         * change.
         * Then we apply the sanitizer.
         * And finally, we unescape once again to get values that the frontend can
         * easily handle.
         */
        var previous = value;
        var unescaped = HtmlUtils.htmlUnescape(previous);
        while (!unescaped.equals(previous)) {
            previous = unescaped;
            unescaped = HtmlUtils.htmlUnescape(previous);
        }
        var sanitized = sanitizer.sanitize(unescaped);
        sanitized = HtmlUtils.htmlUnescape(sanitized);
        // check whether value has effectively changed before doing anything
        if (!sanitized.equals(value)) {
            action.accept(sanitized);
            return Optional.of(sanitized);
        }
        return Optional.empty();
    }

    public List<String> getAttributesExcluded() {
        return attributesExcluded;
    }

    public boolean isSanitizerEnabled() {
        return isEnabled;
    }
}
