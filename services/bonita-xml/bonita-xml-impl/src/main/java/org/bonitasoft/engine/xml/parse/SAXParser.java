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
package org.bonitasoft.engine.xml.parse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.SInvalidSchemaException;
import org.bonitasoft.engine.xml.SValidationException;
import org.bonitasoft.engine.xml.SXMLParseException;
import org.bonitasoft.engine.xml.XMLSchemaValidator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SAXParser implements Parser {

    private static final String UTF_8 = "UTF-8";

    private List<Class<? extends ElementBinding>> bindings;

    private final XMLSchemaValidator validator;

    private final TechnicalLoggerService logger;

    private ElementBindingsFactory bindingsFactory;

    public SAXParser(final XMLSchemaValidator validator, final TechnicalLoggerService logger) {
        this.validator = validator;
        this.logger = logger;
    }

    @Override
    public Object getObjectFromXML(final URI xmlURI) throws SXMLParseException, IOException {
        final File xmlFile = new File(xmlURI);
        return this.getObjectFromXML(xmlFile);
    }

    @Override
    public Object getObjectFromXML(final String pathname) throws SXMLParseException, IOException {
        final File xmlFile = new File(pathname);
        return this.getObjectFromXML(xmlFile);
    }

    @Override
    public Object getObjectFromXML(final File xmlFile) throws SXMLParseException, IOException {
        final FileInputStream fileInputStream = new FileInputStream(xmlFile);
        try {
            return this.getObjectFromXML(fileInputStream);
        } finally {
            fileInputStream.close();
        }
    }

    @Override
    public Object getObjectFromXML(final byte[] bytes) throws SXMLParseException, IOException {
        return getObjectFromXML(new ByteArrayInputStream(bytes));
    }
    @Override
    public Object getObjectFromXML(final InputStream xmlStream) throws SXMLParseException, IOException {
        final InputStreamReader xmlInputStreamReader = new InputStreamReader(xmlStream, Charset.forName(UTF_8));
        try {
            return this.getObjectFromXML(xmlInputStreamReader);
        } finally {
            xmlInputStreamReader.close();
        }
    }

    @Override
    public Object getObjectFromXML(final Reader xmlReader) throws SXMLParseException, IOException {
        final BindingHandler handler;
        if (getBinders() != null) {
            handler = new BindingHandler(getBinders());
        } else {
            handler = new BindingHandler(bindingsFactory);
        }
        final LogErrorHandler errorhandler = new LogErrorHandler(logger);
        try {
            final XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(handler);
            reader.setErrorHandler(errorhandler);
            final InputSource xmlInputSource = new InputSource(xmlReader);
            reader.parse(xmlInputSource);
            return handler.getModel();
        } catch (final SAXException e) {
            throw new SXMLParseException(e);
        }
    }

    @Override
    public List<Class<? extends ElementBinding>> getBinders() {
        return bindings;
    }

    @Override
    public void setBindings(final List<Class<? extends ElementBinding>> bindings) {
        this.bindings = bindings;
    }

    @Override
    public void setSchema(final InputStream xsdInputStream) throws SInvalidSchemaException {
        validator.setSchema(new StreamSource(xsdInputStream));
    }

    @Override
    public void validate(final Reader xmlReader) throws SValidationException, IOException {
        validator.validate(xmlReader);
    }

    @Override
    public void validate(final URI xmlURI) throws SValidationException, IOException {
        final File xmlFile = new File(xmlURI);
        this.validate(xmlFile);
    }

    @Override
    public void validate(final String pathname) throws SValidationException, IOException {
        final File xmlFile = new File(pathname);
        this.validate(xmlFile);
    }

    @Override
    public void validate(final File xmlFile) throws SValidationException, IOException {
        final FileReader xmlReader = new FileReader(xmlFile);
        try {
            this.validate(xmlReader);
        } finally {
            xmlReader.close();
        }
    }

    @Override
    public void validate(final InputStream xmlStream) throws SValidationException, IOException {
        final InputStreamReader xmlInputStreamReader = new InputStreamReader(xmlStream, Charset.forName("utf-8"));
        try {
            this.validate(xmlInputStreamReader);
        } finally {
            xmlInputStreamReader.close();
        }
    }

    public void setBindingsFactory(final ElementBindingsFactory bindingsFactory) {
        this.bindingsFactory = bindingsFactory;
    }

}
