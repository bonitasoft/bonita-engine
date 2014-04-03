/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.commons.io.IOUtil;

/**
 * Created by Vincent Elcrin
 * Date: 03/12/13
 * Time: 08:48
 * 
 * TODO change implementation to use @{java.util.zip.ZipFile}
 */
public class ZipReader {

    private static SecureRandom random = new SecureRandom();

    private final String parent;

    private final String name;

    public ZipReader(final String parent, final String name) {
        this.parent = parent;
        this.name = name;
    }

    public void read(final Reader reader) throws Exception {
        File zip = new File(parent, name);
        File unzipped = new File(parent, generateRandomFileName(name));
        FileInputStream input = null;
        try {
            input = new FileInputStream(zip);
            IOUtil.unzipToFolder(input, unzipped);
            reader.read(zip, unzipped);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtil.deleteDir(unzipped);
        }
    }

    private String generateRandomFileName(final String name) {
        return removeExtension(name, ".zip") + "-" + random.nextLong();
    }

    private String removeExtension(final String name, final String extension) {
        if (name.contains(extension)) {
            return name.replaceAll(extension, "");
        }
        return name;
    }
}
