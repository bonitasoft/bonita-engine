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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for security properties access
 *
 * @author Anthony Birembaut
 */
public class SecurityProperties {

    /**
     * Default name of the form definition file
     */
    public static final String SECURITY_DEFAULT_CONFIG_FILE_NAME = "security-config.properties";

    /**
     * property for the robustness of the password
     */
    public static final String PASSWORD_VALIDATOR_CLASSNAME = "security.password.validator";

    /**
     * property for the CSRF protection activation
     */
    public static final String CSRF_PROTECTION = "security.csrf.enabled";

    /**
     * Property for the (OWASP) Sanitizer protection activation.
     * This sanitizer protects against multiple attacks such as XSS, but may restrict the use of some character
     * sequences.
     */
    public static final String SANITIZER_PROTECTION = "security.sanitizer.enabled";

    /**
     * Property for the (OWASP) Sanitizer protection.
     * The value of this property lists the json attributes that should be excluded when sanitizer protection is active.
     */
    public static final String SANITIZER_PROTECTION_EXCLUSIONS = "security.sanitizer.exclude";

    /**
     * default list of attributes excluded from sanitizer protection when the property is not set in
     * security-config.properties
     */
    public static final List<String> DEFAULT_SANITIZER_PROTECTION_EXCLUSIONS = List.of("email", "password",
            "password_confirm");

    /**
     * property for the CSRF token cookie to have the secure flag (HTTPS only)
     */
    public static final String SECURE_TOKEN_COOKIE = "security.csrf.cookie.secure";

    /**
     * property allowing to set the X-Frame-Options header value in the response
     */
    public static final String X_FRAME_OPTIONS_HEADER = "bonita.runtime.security.csrf.header.frame.options";

    /**
     * property allowing to set the Content-Security-Policy header value in the response
     */
    public static final String CONTENT_SECURITY_POLICY_HEADER = "bonita.runtime.security.csrf.header.content.security.policy";

    /**
     * property for the REST API Authorization checks activation
     */
    public static final String API_AUTHORIZATIONS_CHECK = "security.rest.api.authorizations.check.enabled";

    private static final Map<String, Optional<String>> securityProperties = new ConcurrentHashMap<>();

    /**
     * @return the password validator property
     */
    public String getPasswordValidator() {
        return getTenantProperty(PASSWORD_VALIDATOR_CLASSNAME);
    }

    /**
     * @return the value to allow or not API authorization checks
     */
    public boolean isAPIAuthorizationsCheckEnabled() {
        final String res = getTenantProperty(API_AUTHORIZATIONS_CHECK);
        return res != null && res.equals("true");
    }

    /**
     * @return the value to allow or not CSRF protection
     */
    public boolean isCSRFProtectionEnabled() {
        final String res = getPlatformProperty(CSRF_PROTECTION);
        return res != null && res.equals("true");
    }

    /**
     * @return the value to allow or not Sanitizer activation for protection
     */
    public boolean isSanitizerProtectionEnabled() {
        final String res = getPlatformProperty(SANITIZER_PROTECTION);
        // keep true as default when not set correctly
        return !Boolean.FALSE.toString().equalsIgnoreCase(res);
    }

    /**
     * @return the attributes to exclude from Sanitizer protection, comma separated
     */
    public List<String> getAttributeExcludedFromSanitizerProtection() {
        String excludedAttributes = getPlatformProperty(SANITIZER_PROTECTION_EXCLUSIONS);
        if (excludedAttributes == null) {
            return DEFAULT_SANITIZER_PROTECTION_EXCLUSIONS;
        } else if (excludedAttributes.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(excludedAttributes.trim().split("\\s*,\\s*"));
    }

    /**
     * @return the value to add or not secure flag to the cookies for CSRF token
     */
    public boolean isCSRFTokenCookieSecure() {
        final String res = getPlatformProperty(SECURE_TOKEN_COOKIE);
        return res != null && res.equals("true");
    }

    public String getXFrameOptionsHeader() {
        return getPlatformProperty(X_FRAME_OPTIONS_HEADER);
    }

    public String getContentSecurityPolicyHeader() {
        return getPlatformProperty(CONTENT_SECURITY_POLICY_HEADER);
    }

    protected ConfigurationFilesManager getConfigurationFilesManager() {
        return ConfigurationFilesManager.getInstance();
    }

    public String getTenantProperty(String propertyName) {
        Properties tenantProperties = getConfigurationFilesManager()
                .getTenantProperties(SECURITY_DEFAULT_CONFIG_FILE_NAME);
        Optional<String> propertyValue = securityProperties.get(propertyName);
        if (propertyValue == null) {
            propertyValue = Optional.ofNullable(tenantProperties.getProperty(propertyName));
            securityProperties.put(propertyName, propertyValue);
        }
        return propertyValue.orElse(null);
    }

    public String getPlatformProperty(String propertyName) {
        Properties platformProperties = getConfigurationFilesManager()
                .getPlatformProperties(SECURITY_DEFAULT_CONFIG_FILE_NAME);
        Optional<String> propertyValue = securityProperties.get(propertyName);
        if (propertyValue == null) {
            propertyValue = Optional.ofNullable(platformProperties.getProperty(propertyName));
            securityProperties.put(propertyName, propertyValue);
        }
        return propertyValue.orElse(null);
    }

}
