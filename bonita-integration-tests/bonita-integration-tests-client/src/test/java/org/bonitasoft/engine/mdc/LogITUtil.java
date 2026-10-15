/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class LogITUtil {

    /**
     * Check there is a line in the log which contain context variables
     *
     * @param log the log text
     * @param context context variables to check
     */
    public static void checkLogEntryContains(String log, Map<String, String> context) {
        checkLogEntryContains(log, context, Collections.emptyList());
    }

    /**
     * Check there is a line in the log which contain context variables
     *
     * @param log the log text
     * @param context context variables to check
     * @param forbiddenKeys keys that must not appear in the context
     */
    public static void checkLogEntryContains(String log, Map<String, String> context,
            Collection<String> forbiddenKeys) {
        var lines = log.split("\n\\|");
        assertThat(lines).anyMatch(l -> {
            var entries = context.entrySet().stream();
            return entries.allMatch(e -> {
                // we use the '| %X' in the pattern, so that every variable is preceded by a blank space
                var valueLog = MessageFormat.format(" {0}={1}", e.getKey(), e.getValue());
                return l.contains(valueLog);
            }) && forbiddenKeys.stream().noneMatch(k -> {
                var keyLog = MessageFormat.format(" {0}=", k);
                return l.contains(keyLog);
            });
        });
    }

}
