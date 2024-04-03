/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.EmptyInterceptor;

/**
 * Make search case insensitive in oracle by using upper() function
 */
public class OracleInterceptor extends EmptyInterceptor {

    /*
     * Pattern to find like clauses in sql query :
     * (?<= WHERE | OR | AND ) : preceded by WHERE, OR or AND
     * (?:[ \(]*) : followed by 0 or more spaces or ( (non-capturing group)
     * ((?:(?! WHERE | OR | AND |\\)).)*) : followed by anything except WHERE, OR, AND or ) 0 or more times (capturing
     * group)
     * (\\)?) : followed by 0 or 1 ) (capturing group)
     * ( +LIKE +) : followed by 1 or more spaces, LIKE and 1 or more spaces (capturing group)
     * ((?:[^ ]+(?: *\|\| *[^ ]*)*)) : followed by any character except space 1 or more times
     * and 0 or more times (space, ||, space and 0 or more times any character except space) (capturing group)
     */
    public static final Pattern LIKE_PATTERN = Pattern.compile(
            "(?<= WHERE | OR | AND )(?:[ \\(]*)((?:(?! WHERE | OR | AND |\\)).)*)(\\)?)( +LIKE +)((?:[^ ]+(?: *\\|\\| *[^ ]*)*))",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String onPrepareStatement(final String sql) {
        if (sql.contains("like '") || sql.contains("LIKE '") || sql.contains("like ?") || sql.contains("LIKE ?")) {
            Matcher matcher = LIKE_PATTERN.matcher(sql);
            String newSQL = sql;
            while (matcher.find()) {
                newSQL = newSQL.replace(matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4),
                        "UPPER(" + matcher.group(1) + ")"
                                + matcher.group(2) + matcher.group(3)
                                + "UPPER(" + matcher.group(4) + ")");
            }
            return newSQL;
        }
        return sql;
    }
}
