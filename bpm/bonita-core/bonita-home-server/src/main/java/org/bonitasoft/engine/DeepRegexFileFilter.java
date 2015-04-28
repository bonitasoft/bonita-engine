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
package org.bonitasoft.engine;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.bonitasoft.engine.commons.StringUtils;

/**
 * Identical to {@link org.apache.commons.io.filefilter.RegexFileFilter}, but accept files based on the complete file path and name, not only its name.
 *
 * @author Emmanuel Duchastenier
 */
public class DeepRegexFileFilter extends AbstractFileFilter {

    /**
     * The regular expression pattern that will be used to match filenames
     */
    private final Pattern regExPattern;

    public DeepRegexFileFilter(final String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        regExPattern = Pattern.compile(pattern);
    }

    public DeepRegexFileFilter(final File parentDir, final String pattern) {
        this(StringUtils.uniformizePathPattern(parentDir.getAbsolutePath()) + "/" + pattern);
    }

    @Override
    public boolean accept(final File file) {
        final String fullName = StringUtils.uniformizePathPattern(file.getAbsolutePath());
        return regExPattern.matcher(fullName).matches();
    }

}
