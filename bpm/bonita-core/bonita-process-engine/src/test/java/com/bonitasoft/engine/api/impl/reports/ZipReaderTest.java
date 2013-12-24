/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Vincent Elcrin
 * Date: 03/12/13
 * Time: 13:52
 */
public class ZipReaderTest {

    @Test
    public void should_extract_a_zip_and_expose_content() throws Exception {
        ZipReader zip = new ZipReader("src/test/resources", "myreport-content.zip");

        zip.read(new Reader() {
            @Override
            public void read(File zip, File unzipped) throws Exception {

                assertTrue(unzipped.exists());
                assertEquals("[myreport.properties]", Arrays.asList(unzipped.list()).toString());
            }
        });
    }

    @Test
    public void should_remove_temporary_files_once_finish() throws Exception {
        ZipReader zip = new ZipReader("src/test/resources", "myreport-content.zip");
        final File[] files = {null};

        zip.read(new Reader() {
            @Override
            public void read(File zip, File unzipped) throws Exception {
                files[0] = unzipped;
            }
        });

        assertFalse(files[0].exists());
    }
}
