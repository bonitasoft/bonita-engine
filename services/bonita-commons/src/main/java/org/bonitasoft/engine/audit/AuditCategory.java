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
package org.bonitasoft.engine.audit;

/**
 * The categories of the audit log, the stream of security events read by a SIEM (see ADR-0019). Each category
 * writes to its own child logger of {@value #PARENT_LOGGER_NAME}, named after its {@code event.category}: the
 * logging configuration routes the whole audit log through the parent logger, and can switch off one category.
 * <p>
 * Defined here so that the web tier and the engine share the same logger names. Not related to
 * {@code org.bonitasoft.engine.work.audit}, which monitors the execution of the works.
 */
public enum AuditCategory {

    /** Logins, login failures and logouts. */
    AUTHENTICATION("authentication");

    /**
     * The parent logger of the audit log, routed by the distribution. Its namespace is reserved for the loggers of
     * the categories: no Java package or other logger may use it, as its messages would be written to the audit log.
     */
    public static final String PARENT_LOGGER_NAME = "org.bonitasoft.runtime.audit";

    private final String value;

    AuditCategory(final String value) {
        this.value = value;
    }

    /**
     * @return the {@code event.category} of the events, as defined by the Elastic Common Schema
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the name of the logger the events of this category are written to
     */
    public String getLoggerName() {
        return PARENT_LOGGER_NAME + "." + value;
    }
}
