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

import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.XMLSchemaValidator;
import org.bonitasoft.engine.xml.XMLSchemaValidatorFactory;

/**
 * @author Baptiste Mesta
 */
public class SAXParserFactory implements ParserFactory {

    private final XMLSchemaValidatorFactory validatorFactory;

    private final TechnicalLoggerService logger;

    public SAXParserFactory(final XMLSchemaValidatorFactory validatorFactory, final TechnicalLoggerService logger) {
        this.validatorFactory = validatorFactory;
        this.logger = logger;
    }

    @Override
    public Parser createParser(final List<Class<? extends ElementBinding>> bindings) {
        final XMLSchemaValidator validator = validatorFactory.createValidator();
        final SAXParser saxParser = new SAXParser(validator, logger);
        saxParser.setBindings(bindings);
        return saxParser;
    }

    @Override
    public Parser createParser(final ElementBindingsFactory bindingsFactory) {
        final XMLSchemaValidator validator = validatorFactory.createValidator();
        final SAXParser saxParser = new SAXParser(validator, logger);
        saxParser.setBindingsFactory(bindingsFactory);
        return saxParser;
    }

}
