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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * @author Arthur Freycon
 */
public class CustomListener extends RunListener {

    private static final String OK = "OK";

    private static final String KO = "KO";

    private static final String IGNORE = "IGNORE";

    private String REPORT_FULL_NAME;

    private static final String NODE_TEST = "test";

    private static final String NODE_SUITE = "suite";

    private static final String NODE_TEST_NAME = "test_name";

    private static final String NODE_CLASS = "class";

    private static final String NODE_BPMN_CONCEPT = "BPMN_concept";

    private static final String NODE_STORY = "story";

    private static final String NODE_TRACKER = "tracker_ref";

    private static final String NODE_KEYWORD = "keywords";

    private static final String NODE_STATUS = "status";

    private static final String NODE_DATE = "tested_on";

    private Cover cover;

    private XMLStreamWriter writer;

    private String testState;

    private final SimpleDateFormat sdfReport = new SimpleDateFormat("yyyyMMddHHmm");

    private final SimpleDateFormat sdfTest = new SimpleDateFormat("dd-MM-yyyy, HH:mm:ss, z");

    public CustomListener() {
        System.out.println("Creation of the customized listener");
        createXML();
    }

    private void createXML() {

    }

    @Override
    public void testRunStarted(final Description description) throws Exception {
        try {
            String current = new java.io.File(".").getCanonicalPath();
            System.out.println("Current dir: " + current);
            current = current.replace("\\", "/");
            final String[] wkDir = current.split("/");
            final String shortDir = wkDir[wkDir.length - 1];
            System.out.println("Short dir: " + shortDir);

            REPORT_FULL_NAME = "target/" + shortDir + "_report." + sdfReport.format(System.currentTimeMillis()) + ".xml";

            final File file = new File(REPORT_FULL_NAME);
            System.out.println(file.getAbsolutePath());
            file.createNewFile();
            final XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new BufferedOutputStream(new FileOutputStream(file)),
                    "UTF-8");
            writer = new IndentingXMLStreamWriter(xmlWriter);
            writer.writeStartDocument();
            writer.writeStartElement("TESTS_REPORTS");
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void testRunFinished(final Result result) throws Exception {
        finishXML();

        String path = new File(".").getCanonicalPath();
        path = path.replace("\\", "/");
        System.out.println("Cover annotation report generated at : " + path + "/" + REPORT_FULL_NAME);
    }

    private void finishXML() throws XMLStreamException {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

    private void writeTest(final Description description) throws Exception {
        cover = description.getAnnotation(Cover.class);
        if (cover != null) {
            writer.writeStartElement(NODE_TEST);

            writeField(NODE_SUITE, description.getClassName());
            writeField(NODE_TEST_NAME, description.getMethodName());

            final Class<?>[] classes = cover.classes();
            final BPMNConcept concept = cover.concept();
            final String story = cover.story();
            final String jira = cover.jira();
            final String[] kws = cover.keywords();

            // Classes element
            final StringBuilder builder = new StringBuilder();
            if (classes.length > 0) {
                for (final Class<?> c : classes) {
                    builder.append(c.getName());
                    if (!c.equals(classes[classes.length - 1])) {
                        builder.append(",");
                    }
                }
            }

            writeField(NODE_CLASS, builder.toString());
            writeField(NODE_BPMN_CONCEPT, concept.name());
            writeField(NODE_STORY, story);
            writeField(NODE_TRACKER, jira);

            // Keyword element
            final StringBuilder keyBuilder = new StringBuilder();
            if (kws.length > 0) {
                for (final String k : kws) {
                    keyBuilder.append(k);
                    if (!k.equals(kws[kws.length - 1])) {
                        keyBuilder.append(", ");
                    }
                }
            }
            writeField(NODE_KEYWORD, keyBuilder.toString());
            // test status element
            writer.writeStartElement(NODE_STATUS);
            writer.writeCharacters(testState);
            writer.writeEndElement();

            writer.writeStartElement(NODE_DATE);
            final String strTime = sdfTest.format(System.currentTimeMillis());
            writer.writeCharacters(strTime);
            // writer.writeCharacters(String.valueOf(System.currentTimeMillis()));
            writer.writeEndElement();

            writer.writeEndElement();
        }

    }

    private void writeField(final String tag, final String content) throws XMLStreamException {
        if (content != null && !content.isEmpty()) {
            writer.writeStartElement(tag);
            writer.writeCharacters(content);
            writer.writeEndElement();
        }
    }

    @Override
    public void testFinished(final Description description) throws Exception {
        if (description == null) {
            return;
        }
        if (description.isTest()) {
            writeTest(description);
        }
    }

    @Override
    public void testStarted(final Description description) {
        testState = OK;
    }

    @Override
    public void testFailure(final Failure failure) {
        testState = KO;
    }

    @Override
    public void testIgnored(final Description description) {
        testState = IGNORE;
    }

    @Override
    public void testAssumptionFailure(final Failure failure) {
        testState = KO;
    }

}
