/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.recorder;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.StringUtil;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class QueriableLogSelectDescriptorBuilder {

    public static SelectListDescriptor<SQueriableLog> getLogsFromLongIndex(final String indexName, final long value, final QueryOptions queryOptions) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(indexName, value);
        final StringBuilder stbQueryName = new StringBuilder("getLogsFrom");
        stbQueryName.append(StringUtil.firstCharToUpperCase(indexName));
        return new SelectListDescriptor<SQueriableLog>(stbQueryName.toString(), inputs, SQueriableLog.class, queryOptions);
    }

    public static SelectListDescriptor<SQueriableLog> getLogsFromStringIndex(final String indexName, final String value, final QueryOptions queryOptions) {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(indexName, value);
        final StringBuilder stbQueryName = new StringBuilder("getLogsFrom");
        stbQueryName.append(StringUtil.firstCharToUpperCase(indexName));
        return new SelectListDescriptor<SQueriableLog>(stbQueryName.toString(), inputs, SQueriableLog.class, queryOptions);
    }

}
