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
package org.bonitasoft.engine.bdm.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * @author Romain Bioteau
 */
public class SQLNameValidator {

    private final int maxLength;

    static Set<String> sqlKeywords;
    static {
        sqlKeywords = new HashSet<String>();
    }

    public SQLNameValidator() {
        this(255);
    }

    public SQLNameValidator(final int maxLength) {
        this.maxLength = maxLength;
        if (sqlKeywords.isEmpty()) {
            initializeSQLKeywords();
        }
    }

    private void initializeSQLKeywords() {
        InputStream resourceAsStream = null;
        Scanner scanner = null;
        try {
            resourceAsStream = SQLNameValidator.class.getResourceAsStream("/sql_keywords");
            scanner = new Scanner(resourceAsStream);
            while (scanner.hasNext()) {
                final String word = scanner.nextLine();
                sqlKeywords.add(word.trim());
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public boolean isValid(final String name) {
        return name.matches("[a-zA-Z][\\d\\w#@]{0," + maxLength + "}$") && !isSQLKeyword(name);
    }

    public boolean isSQLKeyword(final String name) {
        return sqlKeywords.contains(name.toUpperCase());
    }

}
