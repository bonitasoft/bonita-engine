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
package org.bonitasoft.engine.xml;

import java.util.Arrays;

/**
 * Configurable XStream deserialization deny list.
 * <p>
 * Provides the list of wildcard patterns used by {@code XStream.denyTypesByWildcard()} to block
 * known deserialization gadget chain libraries and prevent RCE.
 * <p>
 * The deny list can be overridden at runtime via the system property
 * {@value #DENY_PACKAGES_PROPERTY} (comma-separated wildcard patterns).
 * When set, it fully replaces the hardcoded defaults.
 * <p>
 * <b>Important:</b> The system property is read at first XStream usage. It should be set at
 * JVM startup (e.g. via {@code -D} flag) before any serialization/deserialization occurs.
 */
public final class XStreamDenyList {

    public static final String DENY_PACKAGES_PROPERTY = "bonita.xstream.deny.packages";

    private static final String[] DEFAULT_DENY_PATTERNS = {
            "org.apache.commons.collections.functors.**",
            "org.apache.commons.collections4.functors.**",
            "org.apache.commons.collections4.comparators.**",
            "org.apache.commons.beanutils.**",
            "com.sun.org.apache.xalan.**",
            "com.sun.org.apache.bcel.**",
            "javassist.**",
            "groovy.lang.MethodClosure",
            "groovy.lang.GroovyShell",
            "groovy.lang.GroovyClassLoader",
            "org.codehaus.groovy.runtime.**",
            "javax.script.**",
    };

    private XStreamDenyList() {
    }

    /**
     * Returns the deny patterns to use, reading from the system property if set,
     * falling back to hardcoded defaults otherwise.
     *
     * @return a fresh copy of the deny patterns array
     */
    public static String[] getDenyPatterns() {
        String property = System.getProperty(DENY_PACKAGES_PROPERTY);
        if (property != null && !property.isBlank()) {
            return parsePatterns(property);
        }
        return DEFAULT_DENY_PATTERNS.clone();
    }

    private static String[] parsePatterns(String commaDelimited) {
        return Arrays.stream(commaDelimited.trim().split("\\s*,\\s*"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
}
