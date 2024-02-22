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
package org.bonitasoft.web.toolkit.client.data.item.attribute.validator;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.ui.utils.ListUtils;

/**
 * @author Séverin Moussel
 */
public class FileExtensionAllowedValidator extends AbstractStringFormatValidator {

    private final List<String> extensions;

    public FileExtensionAllowedValidator(final String... extensions) {
        super(makeRegexp(extensions));
        this.extensions = Arrays.asList(extensions);
    }

    private static String makeRegexp(final String[] extensions) {
        final StringBuilder sb = new StringBuilder();
        for (final String extension : extensions) {
            sb.append(extension).append("|");
        }
        return "\\.(" + sb.substring(0, sb.length() - 1) + ")$";
    }

    @Override
    protected String defineErrorMessage() {
        return AbstractI18n.t_("%attribute% file format is not allowed. Only %file_formats% files are allowed.",
                new Arg("file_formats",
                        ListUtils.join(this.extensions, ", ", " " + AbstractI18n.t_("or") + " ", "\".", "\"")));

    }
}
