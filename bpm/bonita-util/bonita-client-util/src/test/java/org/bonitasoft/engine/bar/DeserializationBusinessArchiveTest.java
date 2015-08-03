/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.bar;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mazourd
 */
public class DeserializationBusinessArchiveTest {

    private List<File> listOfFiles = new ArrayList<>();
    private File tempFolder;
    private File barFile;

    public void listFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
                listOfFiles.add(fileEntry);
            }
        }
        System.out.println("===============================================================================================");
    }

    @Before
    public void before() throws IOException {
        File folder = new java.io.File("/home/mazourd/work/bonita-engine/processes");
        listFilesForFolder(folder);
        final String barFolderName = "tmpBar";
        tempFolder = IOUtil.createTempDirectoryInDefaultTempDirectory(barFolderName);
        deleteDirOnExit(tempFolder);
        IOUtil.deleteDir(tempFolder);
        barFile = IOUtil.createTempFileInDefaultTempDirectory(barFolderName, ".bar");
        IOUtil.deleteFile(barFile, 2, 3);
    }

    @After
    public void after() throws IOException {
        IOUtil.deleteFile(barFile, 1, 0);
        IOUtil.deleteDir(tempFolder);
    }

    private void deleteDirOnExit(final File directory) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (directory != null) {
                    try {
                        IOUtil.deleteDir(directory);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Test
    public void dummy() {

    }

    @Test
    public void TestAdaptedXml() throws JAXBException {
        for (File file : listOfFiles) {
            if (file.getName().contains("xml")) {
                System.out.println(file.getName());
                JAXBContext jaxbContext = JAXBContext.newInstance(DesignProcessDefinitionImpl.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Object deserializedObject = unmarshaller.unmarshal(file);
                DesignProcessDefinitionImpl process = (DesignProcessDefinitionImpl) deserializedObject;
                System.out.println(process.toString());
            }
        }
    }

    //@Test
    public void ReadTheOldBarsTest() throws IOException, InvalidBusinessArchiveFormatException {
        for (File file : listOfFiles) {
            System.out.println(file.getName());
            BusinessArchiveFactory.readBusinessArchive(file);
        }
    }
}
