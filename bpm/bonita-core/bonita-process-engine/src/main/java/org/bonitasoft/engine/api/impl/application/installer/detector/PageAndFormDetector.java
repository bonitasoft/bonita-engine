/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author Emmanuel Duchastenier
 */
@Component
public class PageAndFormDetector extends CustomPageDetector {

    private static final String PAGE_CONTENT_TYPE = "page";
    private static final String FORM_CONTENT_TYPE = "form";

    private static final String INDEX_GROOVY = "resources/Index.groovy";
    private static final String INDEX_HTML = "resources/index.html";

    public boolean isCompliant(File file) throws IOException {
        return super.isCompliant(file, PAGE_CONTENT_TYPE, FORM_CONTENT_TYPE)
                && (isFilePresent(file, INDEX_HTML) || isFilePresent(file, INDEX_GROOVY));
    }
}
