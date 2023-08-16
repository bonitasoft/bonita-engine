/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.connector.parser;

import java.util.Comparator;

/**
 * Connector implementation comparator depending on a given {@link SConnectorImplementationDescriptor} field.
 * By default, comparison is made on connector implementation id.
 */
public class ConnectorImplementationFieldComparator implements Comparator<SConnectorImplementationDescriptor> {

    /** Field used by the comparator */
    private final String comparedField;
    /** Null-safe comparator on strings to prevent NPE */
    private final Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareTo);

    public ConnectorImplementationFieldComparator(String comparedField) {
        this.comparedField = comparedField;
    }

    @Override
    public int compare(SConnectorImplementationDescriptor o1, SConnectorImplementationDescriptor o2) {
        if (comparedField != null) {
            switch (comparedField) {
                case SConnectorImplementationDescriptor.IMPLEMENTATION_CLASS_NAME:
                    return Comparator.comparing(SConnectorImplementationDescriptor::getImplementationClassName,
                            nullSafeStringComparator).compare(o1, o2);
                case SConnectorImplementationDescriptor.IMPLEMENTATION_VERSION:
                    return Comparator
                            .comparing(SConnectorImplementationDescriptor::getVersion, nullSafeStringComparator)
                            .compare(o1, o2);
                case SConnectorImplementationDescriptor.DEFINITION_ID:
                    return Comparator.comparing(SConnectorImplementationDescriptor::getDefinitionId,
                            nullSafeStringComparator).compare(o1, o2);
                case SConnectorImplementationDescriptor.DEFINITION_VERSION:
                    return Comparator.comparing(SConnectorImplementationDescriptor::getDefinitionVersion,
                            nullSafeStringComparator).compare(o1, o2);
                default:
                    // default compare by id
                    return Comparator.comparing(SConnectorImplementationDescriptor::getId, nullSafeStringComparator)
                            .compare(o1, o2);
            }
        }
        return Comparator.comparing(SConnectorImplementationDescriptor::getId, nullSafeStringComparator)
                .compare(o1, o2);
    }

}
