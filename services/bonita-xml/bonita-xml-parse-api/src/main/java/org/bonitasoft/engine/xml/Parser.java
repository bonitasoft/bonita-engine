/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.List;

/**
 * @author Matthieu Chaffotte
 */
public interface Parser {

    List<Class<? extends ElementBinding>> getBinders();

    void setBindings(List<Class<? extends ElementBinding>> bindings);

    void setSchema(URI xmlURI) throws SInvalidSchemaException;

    void setSchema(String pathname) throws SInvalidSchemaException;

    void setSchema(File xsdSchema) throws SInvalidSchemaException;

    void setSchema(InputStream xmlStream) throws SInvalidSchemaException;

    void setSchema(Reader xmlReader) throws SInvalidSchemaException;

    Object getObjectFromXML(URI xmlURI) throws SXMLParseException, IOException;

    Object getObjectFromXML(String pathname) throws SXMLParseException, IOException;

    Object getObjectFromXML(File xmlFile) throws SXMLParseException, IOException;

    Object getObjectFromXML(InputStream xmlStream) throws SXMLParseException, IOException;

    Object getObjectFromXML(Reader xmlReader) throws SXMLParseException, IOException;

    void validate(URI xmlURI) throws SValidationException, IOException;

    void validate(String pathname) throws SValidationException, IOException;

    void validate(File xmlFile) throws SValidationException, IOException;

    void validate(InputStream xmlStream) throws SValidationException, IOException;

    void validate(Reader xmlReader) throws SValidationException, IOException;

}
