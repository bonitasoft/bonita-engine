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
package org.bonitasoft.engine.test.annotation;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Indent the xml stream writer
 *
 * @author Baptiste Mesta
 */
public class IndentingXMLStreamWriter implements XMLStreamWriter {

    public static final String DEFAULT_INDENT = "  ";

    public static final String NORMAL_END_OF_LINE = "\n";

    private final XMLStreamWriter streamWriter;

    public IndentingXMLStreamWriter(final XMLStreamWriter streamWriter) {
        this.streamWriter = streamWriter;
    }

    /** How deeply nested the current scope is. The root element is depth 1. */
    private int depth = 0; // document scope

    /** stack[depth] indicates what's been written into the current scope. */
    private int[] stack = new int[] { 0, 0, 0, 0 };

    private static final int WROTE_MARKUP = 1;

    private static final int WROTE_DATA = 2;

    private String indent = DEFAULT_INDENT;

    private String newLine = NORMAL_END_OF_LINE;

    /** newLine followed by copies of indent. */
    private char[] linePrefix = null;

    public void setIndent(final String indent) {
        if (!indent.equals(this.indent)) {
            this.indent = indent;
            linePrefix = null;
        }
    }

    public String getIndent() {
        return indent;
    }

    public void setNewLine(final String newLine) {
        if (!newLine.equals(this.newLine)) {
            this.newLine = newLine;
            linePrefix = null;
        }
    }

    public static String getLineSeparator() {
        try {
            return System.getProperty("line.separator");
        } catch (final SecurityException ignored) {
        }
        return NORMAL_END_OF_LINE;
    }

    public String getNewLine() {
        return newLine;
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeStartDocument();
        afterMarkup();
    }

    @Override
    public void writeStartDocument(final String version) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeStartDocument(version);
        afterMarkup();
    }

    @Override
    public void writeStartDocument(final String encoding, final String version) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeStartDocument(encoding, version);
        afterMarkup();
    }

    @Override
    public void writeDTD(final String dtd) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeDTD(dtd);
        afterMarkup();
    }

    @Override
    public void writeProcessingInstruction(final String target) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeProcessingInstruction(target);
        afterMarkup();
    }

    @Override
    public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeProcessingInstruction(target, data);
        afterMarkup();
    }

    @Override
    public void writeComment(final String data) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeComment(data);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(final String localName) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeEmptyElement(localName);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeEmptyElement(namespaceURI, localName);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
        beforeMarkup();
        streamWriter.writeEmptyElement(prefix, localName, namespaceURI);
        afterMarkup();
    }

    @Override
    public void writeStartElement(final String localName) throws XMLStreamException {
        beforeStartElement();
        streamWriter.writeStartElement(localName);
        afterStartElement();
    }

    @Override
    public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
        beforeStartElement();
        streamWriter.writeStartElement(namespaceURI, localName);
        afterStartElement();
    }

    @Override
    public void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
        beforeStartElement();
        streamWriter.writeStartElement(prefix, localName, namespaceURI);
        afterStartElement();
    }

    @Override
    public void writeCharacters(final String text) throws XMLStreamException {
        streamWriter.writeCharacters(text);
        afterData();
    }

    @Override
    public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
        streamWriter.writeCharacters(text, start, len);
        afterData();
    }

    @Override
    public void writeCData(final String data) throws XMLStreamException {
        streamWriter.writeCData(data);
        afterData();
    }

    @Override
    public void writeEntityRef(final String name) throws XMLStreamException {
        streamWriter.writeEntityRef(name);
        afterData();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        beforeEndElement();
        streamWriter.writeEndElement();
        afterEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        try {
            while (depth > 0) {
                writeEndElement(); // indented
            }
        } catch (final Exception ignored) {
        }
        streamWriter.writeEndDocument();
        afterEndDocument();
    }

    protected void beforeMarkup() {
        final int soFar = stack[depth];
        // no data in this scope and not the first line
        if ((soFar & WROTE_DATA) == 0 && (depth > 0 || soFar != 0)) {
            try {
                writeNewLine(depth);
                if (depth > 0 && getIndent().length() > 0) {
                    afterMarkup(); // indentation was written
                }
            } catch (final Exception e) {
            }
        }
    }

    protected void afterMarkup() {
        stack[depth] |= WROTE_MARKUP;
    }

    protected void afterData() {
        stack[depth] |= WROTE_DATA;
    }

    protected void beforeStartElement() {
        beforeMarkup();
        if (stack.length <= depth + 1) {
            // Allocate more space for the stack:
            final int[] newStack = new int[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[depth + 1] = 0; // nothing written yet
    }

    protected void afterStartElement() {
        afterMarkup();
        ++depth;
    }

    protected void beforeEndElement() {
        if (depth > 0 && stack[depth] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(depth - 1);
            } catch (final Exception ignored) {
            }
        }
    }

    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
        }
    }

    protected void afterEndDocument() {
        if (stack[depth = 0] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(0);
            } catch (final Exception ignored) {
            }
        }
        stack[depth] = 0; // start fresh
    }

    protected void writeNewLine(final int indentation) throws XMLStreamException {
        final int newLineLength = getNewLine().length();
        final int prefixLength = newLineLength + getIndent().length() * indentation;
        if (prefixLength > 0) {
            if (linePrefix == null) {
                linePrefix = (getNewLine() + getIndent()).toCharArray();
            }
            while (prefixLength > linePrefix.length) {
                // make linePrefix longer:
                final char[] newPrefix = new char[newLineLength + (linePrefix.length - newLineLength) * 2];
                System.arraycopy(linePrefix, 0, newPrefix, 0, linePrefix.length);
                System.arraycopy(linePrefix, newLineLength, newPrefix, linePrefix.length, linePrefix.length - newLineLength);
                linePrefix = newPrefix;
            }
            streamWriter.writeCharacters(linePrefix, 0, prefixLength);
        }
    }

    @Override
    public Object getProperty(final String name) throws IllegalArgumentException {
        return streamWriter.getProperty(name);
    }

    @Override
    public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
        streamWriter.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return streamWriter.getNamespaceContext();
    }

    @Override
    public void setDefaultNamespace(final String uri) throws XMLStreamException {
        streamWriter.setDefaultNamespace(uri);
    }

    @Override
    public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
        streamWriter.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException {
        streamWriter.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public String getPrefix(final String uri) throws XMLStreamException {
        return streamWriter.getPrefix(uri);
    }

    @Override
    public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
        streamWriter.setPrefix(prefix, uri);
    }

    @Override
    public void writeAttribute(final String localName, final String value) throws XMLStreamException {
        streamWriter.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException {
        streamWriter.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException {
        streamWriter.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void flush() throws XMLStreamException {
        streamWriter.flush();
    }

    @Override
    public void close() throws XMLStreamException {
        streamWriter.close();
    }

}
