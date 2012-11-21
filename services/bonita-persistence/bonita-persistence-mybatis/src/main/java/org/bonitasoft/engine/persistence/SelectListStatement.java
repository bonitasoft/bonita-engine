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
package org.bonitasoft.engine.persistence;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Charles Souillard
 */
public class SelectListStatement<T> extends ReadStatement<List<T>> {

    private final RowBounds rowBounds;

    public SelectListStatement(final String statement, final Object parameter, final RowBounds rowBounds) {
        super(statement, parameter);
        this.rowBounds = rowBounds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> execute(final SqlSession sqlSession) {
        return sqlSession.selectList(this.statement, this.parameter, this.rowBounds);
    }

}
