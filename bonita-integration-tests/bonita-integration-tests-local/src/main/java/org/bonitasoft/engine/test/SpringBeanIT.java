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
package org.bonitasoft.engine.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class SpringBeanIT {

    private static final String BONITA_HOME = "bonita.home";

    final BidiMap beansMap = new DualHashBidiMap();

    @Test
    public void checkUniquenessOfSpringBeans() throws DocumentException, Exception {
        final String bonitaHome = System.getProperty(BONITA_HOME);

        // Browse bonitaHome, and check that all spring beans are unique.
        iterateOnDirectory(new File(bonitaHome));
    }

    private Document parse(final File file) throws DocumentException, IOException {
        return DocumentHelper.parseText(FileUtils.readFileToString(file));
    }

    private void findAllBeansOfDocument(final Document document) throws Exception {
        final List<Element> list = document.getRootElement().elements("bean");

        for (final Iterator<Element> iter = list.iterator(); iter.hasNext();) {
            final Element attribute = iter.next();
            final String id = attribute.attributeValue("id");
            final String clazz = attribute.attributeValue("class");

            if (id != null && !beansMap.containsKey(id)) {
                beansMap.put(id, clazz);
            } else if (id != null && beansMap.containsKey(id)) {
                throw new Exception("You want create the bean with id " + id + " and class " + clazz + ", but already exist a bean with id " + id
                        + " and class " + beansMap.get(id) + ".");
            }
        }
    }

    private void iterateOnDirectory(final File file) throws DocumentException, Exception {
        if (file.exists()) {
            if (file.isDirectory() && !"tenants".equals(file.getName())) {
                final List<File> subdirs = Arrays.asList(file.listFiles());
                for (final File subdir : subdirs) {
                    iterateOnDirectory(subdir);
                }
            } else if (file.isFile() && (file.getName().startsWith("bonita") || file.getName().startsWith("cfg")) && file.getName().endsWith(".xml")) {
                findAllBeansOfDocument(parse(file));
            }
        }
    }

}
