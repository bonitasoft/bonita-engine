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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;

/**
 * The base class for our Mapped Diagnostic Contexts (MDC).
 *
 * @author Vincent Hemery
 */
public abstract class AbstractMDC implements AutoCloseable, MDCConstants {

    private Map<String, String> entriesToRestore;

    public AbstractMDC(Map<String, String> contextValues) {
        if (contextValues != null) {
            /*
             * We do not prevent nested MDCs with conflicting values,
             * as it may be legit e.g. in case of an ActivityCall
             * (which executes other flow nodes inside the parent flow node).
             * So we allow erasing any value in the context,
             * but also remember to restore the context in its original state at closure.
             */
            // remember entries to restore/remove
            Map<String, String> oldMap = Optional.ofNullable(MDC.getCopyOfContextMap())
                    .orElseGet(Collections::emptyMap);
            entriesToRestore = new HashMap<>(contextValues.size());
            contextValues.forEach((k, newValue) -> {
                var valueToRestore = oldMap.getOrDefault(k, null);
                entriesToRestore.put(k, valueToRestore);
            });
            // store new values
            contextValues.forEach((key, value) -> {
                if (value != null) {
                    MDC.put(key, value);
                }
            });
        } else {
            entriesToRestore = Collections.emptyMap();
        }
    }

    @Override
    public void close() {
        // restore previous context
        entriesToRestore.forEach((k, v) -> {
            if (v == null) {
                MDC.remove(k);
            } else {
                MDC.put(k, v);
            }
        });
    }

}
