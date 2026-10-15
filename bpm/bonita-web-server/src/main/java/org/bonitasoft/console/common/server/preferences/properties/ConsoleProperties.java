/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.preferences.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bonitasoft.console.common.server.utils.DocumentUtil;

/**
 * @author Yang zhiheng
 */
public class ConsoleProperties {

    /**
     * Document max size
     */
    private static final String ATTACHMENT_MAX_SIZE = "form.attachment.max.size";

    /**
     * Image upload max size
     */
    private static final String IMAGE_UPLOAD_MAX_SIZE = "image.upload.max.size";

    /**
     * Custom page and rest api ext debug mode
     */
    private static final String CUSTOM_PAGE_DEBUG = "custom.page.debug";

    /**
     * time between two database check of custom page and rest api last update date in milliseconds
     */
    private static final String PAGE_LAST_UPDATE_CHECK_INTERVAL_MILLIS = "custom.page.lastupdate.database.check.interval.milliseconds";

    //Default time between two database check of custom page and rest api last update date in milliseconds
    private static final int DEFAULT_PAGE_LAST_UPDATE_CHECK_INTERVAL_MILLIS = 3000;

    /**
     * Whitelist of file extensions accepted by generic file upload widgets/forms
     */
    private static final String ALLOWED_ATTACHMENT_EXTENSIONS = "form.attachment.allowed.extensions";

    // ponytail: functional whitelist scoped to common business document/image formats; extend via
    // form.attachment.allowed.extensions if the business needs others.
    private static final List<String> DEFAULT_ALLOWED_ATTACHMENT_EXTENSIONS = DocumentUtil.DEFAULT_ALLOWED_EXTENSIONS;

    /** Name of attribute attached to the request to indicate the request id */
    private static final String REQUEST_ID_ATTRIBUTE_NAME = "req.requestId.attributeName";
    private static final String DEFAULT_REQUEST_ID_ATTRIBUTE_NAME = "track.requestId";
    /** Name of request header containing the request id when not already attached */
    private static final String REQUEST_ID_HEADER_NAME = "req.requestId.headerName";
    private static final String DEFAULT_REQUEST_ID_HEADER_NAME = "X-Request-ID";
    /** Name of attribute attached to the request to indicate the correlation id */
    private static final String CORRELATION_ID_ATTRIBUTE_NAME = "req.correlationId.attributeName";
    private static final String DEFAULT_CORRELATION_ID_ATTRIBUTE_NAME = "track.correlationId";
    /** Name of request header containing the correlation id when not already attached */
    private static final String CORRELATION_ID_HEADER_NAME = "req.correlationId.headerName";
    private static final String DEFAULT_CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";

    private static final String PROPERTIES_FILE = "console-config.properties";

    private static Map<String, Optional<String>> consoleProperties;

    public Properties getProperties() {
        return ConfigurationFilesManager.getInstance().getTenantProperties(PROPERTIES_FILE);
    }

    public long getMaxSize() {
        final String maxSize = this.getProperty(ATTACHMENT_MAX_SIZE);
        if (maxSize != null) {
            return Long.valueOf(maxSize);
        }
        return 15;
    }

    public long getImageMaxSizeInKB() {
        final String maxSize = this.getProperty(IMAGE_UPLOAD_MAX_SIZE);
        if (maxSize != null) {
            return Long.valueOf(maxSize);
        }
        return 100;
    }

    public List<String> getAllowedAttachmentExtensions() {
        final String extensions = this.getProperty(ALLOWED_ATTACHMENT_EXTENSIONS);
        if (extensions == null || extensions.isBlank()) {
            return DEFAULT_ALLOWED_ATTACHMENT_EXTENSIONS;
        }
        final List<String> configuredExtensions = Arrays.stream(extensions.split(","))
                .map(String::trim)
                .filter(extension -> !extension.isEmpty())
                .collect(Collectors.toList());
        // a non-blank but malformed value (e.g. only commas) must not be treated as "no restriction"
        return configuredExtensions.isEmpty() ? DEFAULT_ALLOWED_ATTACHMENT_EXTENSIONS : configuredExtensions;
    }

    public boolean isPageInDebugMode() {
        final String debugMode = this.getProperty(CUSTOM_PAGE_DEBUG);
        return Boolean.parseBoolean(debugMode);
    }

    public long getPageLastUpdateCheckInterval() {
        final String pageLastUpdateCheckInterval = this.getProperty(PAGE_LAST_UPDATE_CHECK_INTERVAL_MILLIS);
        if (pageLastUpdateCheckInterval != null) {
            return Long.valueOf(pageLastUpdateCheckInterval);
        }
        return DEFAULT_PAGE_LAST_UPDATE_CHECK_INTERVAL_MILLIS;
    }

    public String getRequestIdAttributeName() {
        return Optional.ofNullable(this.getProperty(REQUEST_ID_ATTRIBUTE_NAME))
                .orElse(DEFAULT_REQUEST_ID_ATTRIBUTE_NAME);
    }

    public String getRequestIdHeaderName() {
        return Optional.ofNullable(this.getProperty(REQUEST_ID_HEADER_NAME)).orElse(DEFAULT_REQUEST_ID_HEADER_NAME);
    }

    public String getCorrelationIdAttributeName() {
        return Optional.ofNullable(this.getProperty(CORRELATION_ID_ATTRIBUTE_NAME))
                .orElse(DEFAULT_CORRELATION_ID_ATTRIBUTE_NAME);
    }

    public String getCorrelationIdHeaderName() {
        return Optional.ofNullable(this.getProperty(CORRELATION_ID_HEADER_NAME))
                .orElse(DEFAULT_CORRELATION_ID_HEADER_NAME);
    }

    public String getProperty(String propertyName) {
        if (consoleProperties == null) {
            consoleProperties = new ConcurrentHashMap<>();
        }
        Optional<String> propertyValue = consoleProperties.get(propertyName);
        if (propertyValue == null) {
            propertyValue = Optional.ofNullable(getProperties().getProperty(propertyName));
            consoleProperties.put(propertyName, propertyValue);
        }
        return propertyValue.orElse(null);
    }
}
