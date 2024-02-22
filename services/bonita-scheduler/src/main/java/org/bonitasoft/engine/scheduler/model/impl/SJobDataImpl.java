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
package org.bonitasoft.engine.scheduler.model.impl;

import java.util.Map.Entry;

import lombok.Data;
import org.bonitasoft.engine.scheduler.model.SJobData;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@Data
public class SJobDataImpl implements SJobData {

    private final String key;
    private final Object value;
    final String classOfValue;

    public SJobDataImpl(final Entry<String, Object> jobData) {
        key = jobData.getKey();
        value = jobData.getValue();
        classOfValue = value.getClass().getName();
    }

}
