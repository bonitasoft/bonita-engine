/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.comment.model.builder.impl;

import org.bonitasoft.engine.core.process.comment.model.builder.SCommentLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class SCommentLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SCommentLogBuilderFactory {

    public static final String COMMENT = "COMMENT";

    public static final String COMMENT_INDEX_NAME = "numericIndex1";

    @Override
    public String getObjectIdKey() {
        return COMMENT_INDEX_NAME;
    }

}
