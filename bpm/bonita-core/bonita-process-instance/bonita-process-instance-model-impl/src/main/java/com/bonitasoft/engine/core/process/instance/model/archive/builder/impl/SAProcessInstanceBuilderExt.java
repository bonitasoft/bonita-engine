/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAProcessInstanceBuilderImpl;

import com.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;

/**
 * @author Celine Souchet
 */
public class SAProcessInstanceBuilderExt extends SAProcessInstanceBuilderImpl implements SAProcessInstanceBuilder {

    private static final String STRING_INDEX_1 = "stringIndex1";

    private static final String STRING_INDEX_2 = "stringIndex2";

    private static final String STRING_INDEX_3 = "stringIndex3";

    private static final String STRING_INDEX_4 = "stringIndex4";

    private static final String STRING_INDEX_5 = "stringIndex5";

    @Override
    public String getStringIndex1Key() {
        return STRING_INDEX_1;
    }

    @Override
    public String getStringIndex2Key() {
        return STRING_INDEX_2;
    }

    @Override
    public String getStringIndex3Key() {
        return STRING_INDEX_3;
    }

    @Override
    public String getStringIndex4Key() {
        return STRING_INDEX_4;
    }

    @Override
    public String getStringIndex5Key() {
        return STRING_INDEX_5;
    }

}
