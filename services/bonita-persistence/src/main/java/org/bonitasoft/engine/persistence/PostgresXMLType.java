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

import java.io.Serializable;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class PostgresXMLType
        extends AbstractSingleColumnStandardBasicType<Serializable> {

    public static final PostgresXMLType INSTANCE = new PostgresXMLType();

    public PostgresXMLType() {
        // forcing VARCHAR to String as there is no real CLOB in PSQL
        super(VarcharTypeDescriptor.INSTANCE, new XMLTypeDescriptor());
    }

    public String getName() {
        return "xml_blob";
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }

}
