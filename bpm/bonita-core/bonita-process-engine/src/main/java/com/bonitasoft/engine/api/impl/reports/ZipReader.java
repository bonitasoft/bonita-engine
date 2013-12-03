/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bonitasoft.engine.api.impl.reports;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.io.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;

/**
 * Created by Vincent Elcrin
 * Date: 03/12/13
 * Time: 08:48
 */
public class ZipReader {

    private static SecureRandom random = new SecureRandom();

    private String parent;

    private String name;

    public ZipReader(String parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public void read(Reader reader) throws Exception {
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

    private String generateRandomFileName(String name) {
        return removeExtension(name, ".zip") +  "-" + random.nextLong();
    }

    private String removeExtension(String name, String extension) {
        if(name.contains(extension)) {
            return name.replaceAll(extension, "");
        }
        return name;
    }
}
