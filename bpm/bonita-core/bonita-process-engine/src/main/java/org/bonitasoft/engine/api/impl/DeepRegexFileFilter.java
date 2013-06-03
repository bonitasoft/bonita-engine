/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.RegexFileFilter;

/**
 * Identical to RegexFileFilter, but accept files based on the complete file path and name, not only name.
 * 
 * @author Emmanuel Duchastenier
 */
public class DeepRegexFileFilter extends RegexFileFilter {

    private static final long serialVersionUID = -8894250238822765740L;

    /**
     * The regular expression pattern that will be used to match filenames
     */
    private final Pattern pattern;

    public DeepRegexFileFilter(final String pattern) {
        super(pattern);
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean accept(final File dir, final String name) {
        String fullName = dir.getAbsolutePath() + File.separator + name;
        fullName = fullName.replaceAll("\\\\", "/");
        return pattern.matcher(fullName).matches();
    }

}
