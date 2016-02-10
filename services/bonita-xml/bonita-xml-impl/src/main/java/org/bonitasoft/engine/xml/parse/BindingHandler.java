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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.bonitasoft.engine.xml.ElementBinding;
import org.bonitasoft.engine.xml.ElementBindingsFactory;
import org.bonitasoft.engine.xml.SXMLParseException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Matthieu Chaffotte
 */
public class BindingHandler extends DefaultHandler {

    private final Map<String, Class<? extends ElementBinding>> binders;

    private final ElementBindingsFactory bindingsFactory;

    private final Stack<ElementBinding> model;

    private final Stack<String> elements;

    private StringBuilder tempVal;

    private Map<String, String> tempAttributes;

    public BindingHandler(final List<Class<? extends ElementBinding>> binders) {
        this.binders = this.setBinders(binders);
        model = new Stack<ElementBinding>();
        elements = new Stack<String>();
        tempAttributes = new HashMap<String, String>();
        bindingsFactory = null;
    }

    public BindingHandler(final ElementBindingsFactory bindingsFactory) {
        this.bindingsFactory = bindingsFactory;
        model = new Stack<ElementBinding>();
        elements = new Stack<String>();
        tempAttributes = new HashMap<String, String>();
        binders = this.setBinders(bindingsFactory);
    }

    private Map<String, Class<? extends ElementBinding>> setBinders(final ElementBindingsFactory bindingsFactory) {
        final List<ElementBinding> elementBindings = bindingsFactory.getElementBindings();

        final Map<String, Class<? extends ElementBinding>> temp = new HashMap<String, Class<? extends ElementBinding>>();
        for (int i = 0; i < elementBindings.size(); i++) {
            final ElementBinding elementBinding = elementBindings.get(i);
            try {
                final String tag = elementBinding.getElementTag();
                temp.put(tag, elementBinding.getClass());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    private Map<String, Class<? extends ElementBinding>> setBinders(final List<Class<? extends ElementBinding>> binders) {
        final Map<String, Class<? extends ElementBinding>> temp = new HashMap<String, Class<? extends ElementBinding>>();
        for (int i = 0; i < binders.size(); i++) {
            final Class<? extends ElementBinding> binderClass = binders.get(i);
            try {
                final String tag = binderClass.newInstance().getElementTag();
                temp.put(tag, binderClass);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    public Object getModel() {
        final ElementBinding root = model.peek();
        return root.getObject();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (tempVal == null) {
            tempVal = new StringBuilder();
        }
        String tempString = new String(ch, start, length);
        if ("".equals(tempString.trim())) {
            tempString = "";
        }
        tempVal.append(tempString);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        final Class<? extends ElementBinding> binderClass = binders.get(localName);
        if (binderClass != null) {
            final Map<String, String> elementAttributes = new HashMap<String, String>();
            for (int i = 0; i < attributes.getLength(); i++) {
                final String attributeLocalName = attributes.getLocalName(i);
                final String attributeValue = attributes.getValue(i);
                elementAttributes.put(attributeLocalName, attributeValue);
            }
            final ElementBinding binder = getBinder(binderClass);
            binder.setBinders(model);
            model.push(binder);
            elements.push(localName);
            try {
                binder.setAttributes(elementAttributes);
            } catch (final SXMLParseException e) {
                throw new SAXException(e);
            }
        } else {
            tempAttributes = new HashMap<String, String>();
            for (int i = 0; i < attributes.getLength(); i++) {
                final String attributeLocalName = attributes.getLocalName(i);
                final String attributeValue = attributes.getValue(i);
                tempAttributes.put(attributeLocalName, attributeValue);
            }
        }
    }

    private ElementBinding getBinder(final Class<? extends ElementBinding> binderClass) throws SAXException {
        try {
            if (bindingsFactory != null) {
                return bindingsFactory.createNewInstance(binderClass);
            }
            return binderClass.newInstance();
        } catch (final Exception e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        String currentElement = elements.peek();
        if (localName.equals(currentElement)) {
            final ElementBinding child = model.pop();
            currentElement = elements.pop();
            final Object object = child.getObject();
            if (!model.isEmpty()) {
                final ElementBinding parent = model.peek();
                try {
                    parent.setChildObject(localName, object);
                } catch (final SXMLParseException e) {
                    throw new SAXException(e);
                }
            } else {
                model.push(child);
                elements.push(currentElement);
            }
        } else {
            final ElementBinding binder = model.peek();
            try {
                if (tempVal != null) {
                    binder.setChildElement(localName, tempVal.toString(), tempAttributes);
                } else {
                    binder.setChildElement(localName, "", tempAttributes);
                }
                tempVal = null;
            } catch (final SXMLParseException e) {
                throw new SAXException(e);
            }
        }
    }

}
