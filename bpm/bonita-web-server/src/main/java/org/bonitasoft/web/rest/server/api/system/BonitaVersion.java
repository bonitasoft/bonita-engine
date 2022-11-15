/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.system;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vincent Elcrin
 */
public class BonitaVersion {

    private static final Logger LOGGER = LoggerFactory.getLogger(BonitaVersion.class.getName());

    private List<String> metadata;

    private final VersionFile versionFile;

    public BonitaVersion(final VersionFile file) {
        this.versionFile = file;
    }

    private List<String> read(VersionFile versionFile) {
        List<String> result = new ArrayList<>();
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(versionFile.getStream())).lines()) {
            result = lines.collect(Collectors.toList());
        } catch (final Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to read the file VERSION", e);
            }
        }
        return result;

    }

    public String getVersion() {
        if (metadata == null) {
            metadata = read(versionFile);
        }
        if (metadata.size() > 0) {
            return metadata.get(0).trim();
        } else {
            return "";
        }
    }

    public String getBrandingVersion() {
        String brandingVersionWithDate = getBrandingVersionWithUpdate();

        return brandingVersionWithDate.substring(0, brandingVersionWithDate.indexOf("-"));
    }

    public String getBrandingVersionWithUpdate() {
        if (metadata == null) {
            metadata = read(versionFile);
        }
        if (metadata.size() > 1) {
            return metadata.get(1).trim();
        } else {
            return "";
        }
    }

    public String getCopyright() {
        if (metadata == null) {
            metadata = read(versionFile);
        }
        if (metadata.size() > 2) {
            return metadata.get(2).trim();
        } else {
            return "";
        }
    }
}
