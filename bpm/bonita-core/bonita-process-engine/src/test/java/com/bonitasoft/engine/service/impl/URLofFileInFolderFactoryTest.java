/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

public class URLofFileInFolderFactoryTest {

    @Test
    public void should_create_instance_with_relatif_file_path() throws Exception {
        // given
        File tempFile = new File("testtttt");
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write("test");
        fileWriter.close();
        URLofFileInFolderFactory factory = createFactory("", tempFile.getName());

        // when
        String url = factory.createInstance();

        tempFile.delete();
        // then
        assertThat(url).isEqualTo(tempFile.toURI().toURL().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void should_create_instance_with__unexisting_file_throw_exception() throws Exception {
        // given
        File currentDir = new File("");
        URLofFileInFolderFactory factory = createFactory(currentDir.getAbsolutePath(), "testtttttt");

        // when
        factory.createInstance();
    }

    @Test
    public void should_create_instance_with_file_return_url() throws Exception {
        // given
        File tempFile = File.createTempFile("prefix", ".tmp");
        URLofFileInFolderFactory factory = createFactory(tempFile.getParentFile().getAbsolutePath(), tempFile.getName());

        // when
        String url = factory.createInstance();

        tempFile.delete();
        // then
        assertThat(url).isEqualTo(tempFile.toURI().toURL().toString());
    }

    private URLofFileInFolderFactory createFactory(final String baseFolder, final String filePath) {
        URLofFileInFolderFactory factory = new URLofFileInFolderFactory();
        factory.setBaseFolder(baseFolder);
        factory.setFilePath(filePath);
        return factory;
    }
}
