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

import org.apache.ibatis.session.SqlSession;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Charles Souillard
 * @author Frederic Bouquet
 */
public class InsertStatement extends WriteStatement {

    private final PersistentObject entity;

    public InsertStatement(final String statement, final Object parameter, final PersistentObject entity) {
        super(statement, parameter);
        this.entity = entity;
    }

    @Override
    public Void execute(final SqlSession sqlSession) throws SPersistenceException {
        sqlSession.insert(this.statement, this.parameter);
        return null;
    }

    public PersistentObject getEntity() {
        return this.entity;
    }

}
