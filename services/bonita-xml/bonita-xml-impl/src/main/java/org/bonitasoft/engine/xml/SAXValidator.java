/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SAXValidator implements XMLSchemaValidator {

    private final SchemaFactory factory;

    private Schema schema;

    public SAXValidator() {
        factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

    @Override
    public void setSchema(final StreamSource source) throws SInvalidSchemaException {
        try {
            schema = factory.newSchema(source);
        } catch (final SAXException saxe) {
            throw new SInvalidSchemaException(saxe);
        }
    }

    @Override
    public void validate(final InputStream stream) throws SValidationException, IOException {
        final StreamSource source = new StreamSource(stream);
        validate(source);
    }

    @Override
    public void validate(final String filePath) throws SValidationException, IOException {
        final StreamSource source = new StreamSource(filePath);
        validate(source);
    }

    @Override
    public void validate(final File file) throws SValidationException, IOException {
        // BS-9304 : If you create a new StreamSource with a file, the streamSource keeps a lock on the file when there is an exception.
        // If the file is temporary, the temporary file is never deleted at the end of the jvm, even if you call all methods to delete it.
        // So you need to use the InputStream to close it, even if there is an exception, to unlock the file.
        final InputStream openStream = file.toURI().toURL().openStream();
        try {
            validate(new StreamSource(openStream));
        } finally {
            openStream.close();
        }
    }

    @Override
    public void validate(final Reader reader) throws SValidationException, IOException {
        final StreamSource source = new StreamSource(reader);
        validate(source);
    }

    private void validate(final StreamSource source) throws SValidationException, IOException {
        try {
            if (schema == null) {
                throw new SValidationException("No schema defined");
            }
            final Validator validator = this.schema.newValidator();
            validator.validate(source);
        } catch (final SAXException e) {
            throw new SValidationException(e);
        }
    }

}
