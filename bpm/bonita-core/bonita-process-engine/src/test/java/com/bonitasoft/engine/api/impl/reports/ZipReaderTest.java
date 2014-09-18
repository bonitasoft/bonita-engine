/*******************************************************************************
 * Copyright (C) 2013-2014 Bonitasoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Vincent Elcrin
 * @author Celine Souchet
 */
public class ZipReaderTest {

    @Test
    public void should_extract_a_zip_and_expose_content() throws Exception {
        final ZipReader zip = new ZipReader("src/test/resources/reports", "myreport-content.zip");

        zip.read(new Reader() {

            @Override
            public void read(final File zip, final File unzipped) {
                assertThat(unzipped).exists();
                assertThat(Arrays.asList(unzipped.list()).toString()).isEqualTo("[myreport.properties]");
            }
        });
    }

    @Test
    public void should_remove_temporary_files_once_finish() throws Exception {
        final ZipReader zip = new ZipReader("src/test/resources/reports", "myreport-content.zip");
        final File[] files = { null };

        zip.read(new Reader() {

            @Override
            public void read(final File zip, final File unzipped) {
                files[0] = unzipped;
            }
        });

        assertThat(files[0]).doesNotExist();
    }
}
