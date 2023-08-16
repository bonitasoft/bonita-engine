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

import java.net.URL;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.xml.parser.AbstractParser;

/**
 * Abstract class to define common configuration between connector implementation parsers.
 *
 * @param <T> could be an instance of {@link SConnectorImplementationDescriptor} or
 *        {@link org.bonitasoft.engine.core.filter.model.UserFilterImplementationDescriptor}
 */
public abstract class AbstractConnectorImplementationParser<T> extends AbstractParser<T> {

    public static final String CONNECTOR_IMPLEMENTATION_NAMESPACE = "http://www.bonitasoft.org/ns/connector/implementation/6.0";
    private static final String CONNECTOR_IMPLEMENTATION_XSD = "/connectors-impl.xsd";

    @Override
    protected URL initSchemaURL() {
        // TODO Replace with code below when moved to bonita-artifacts-model
        // return Optional.ofNullable(ResourceFinder.findEntry(CONNECTOR_IMPLEMENTATION_XSD))
        //         .orElseGet(() -> AbstractConnectorImplementationParser.class.getResource(CONNECTOR_IMPLEMENTATION_XSD));
        return AbstractConnectorImplementationParser.class.getResource(CONNECTOR_IMPLEMENTATION_XSD);
    }

    @Override
    public T convert(String xml) throws JAXBException {
        // before converting, add namespace to the xml if it is missing
        return super.convert(addMissingNamespaceToXml(xml));
    }

    /**
     * Add connector implementation namespace to given XML content if the namespace is missing.
     *
     * @param xml XML content
     * @return XML content with connector implementation namespace
     */
    private static String addMissingNamespaceToXml(final String xml) {
        return xml
                .replace("<connectorImplementation>",
                        "<implementation:connectorImplementation xmlns:implementation=\""
                                + CONNECTOR_IMPLEMENTATION_NAMESPACE + "\">")
                .replace("</connectorImplementation>", "</implementation:connectorImplementation>");
    }

}
