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
package org.bonitasoft.engine.persistence;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;

/**
 * Custom Hibernate type for PostgreSQL TEXT fields.
 * In Hibernate 5.6+, PostgreSQL CLOB mapping changed from TEXT to OID type,
 * which causes issues with large text storage. This type forces TEXT columns
 * by using LongVarcharTypeDescriptor which maps to TEXT in PostgreSQL.
 *
 * @author Guillaume Rosinosky
 */
public class PostgresMaterializedClobType extends AbstractSingleColumnStandardBasicType<String> {

    public PostgresMaterializedClobType() {
        // Use LongVarcharTypeDescriptor which maps to TEXT type in PostgreSQL (not OID)
        super(LongVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "materialized_clob";
    }

}
