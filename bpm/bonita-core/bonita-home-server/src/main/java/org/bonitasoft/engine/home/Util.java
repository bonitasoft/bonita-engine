package org.bonitasoft.engine.home;

import java.io.File;

import org.bonitasoft.engine.commons.StringUtils;

/**
 * @author Charles Souillard
 */
public class Util {

    public static String generateRelativeResourcePath(final File folder, final File file) {
        String path = file.getAbsolutePath().replace(folder.getAbsolutePath(), "");
        path = StringUtils.uniformizePathPattern(path);
        // remove first slash, if any:
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
