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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSession;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class MybatisSession {

    private final SqlSession sqlSession;

    private final boolean cacheEnabled;

    private List<WriteStatement> writeStatements;

    private MybatisSessionCache cache;

    public MybatisSession(final SqlSession sqlSession, final boolean cacheEnabled) {
        super();
        this.sqlSession = sqlSession;
        this.cacheEnabled = cacheEnabled;
        if (cacheEnabled) {
            writeStatements = new ArrayList<WriteStatement>();
            cache = new MybatisSessionCache();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T executeSelectOneStatement(final SelectOneStatement<T> statement) throws SPersistenceException {
        try {
            final T result = statement.execute(sqlSession);
            if (cacheEnabled && result != null) {
                if (!PersistentObject.class.isAssignableFrom(result.getClass())) {
                    return result;
                }
                return (T) cache.getOrSave((PersistentObject) result);
            }
            return result;
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T executeSelectByIdStatement(final SelectByIdStatement<T> statement) throws SPersistenceException {
        try {
            if (cacheEnabled) {
                // check cache first
                final T cachedResult = (T) cache.get(statement.getEntityClass(), statement.getId());
                if (cachedResult != null) {
                    return cachedResult;
                }
            }
            return statement.execute(sqlSession);
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> executeSelectListStatement(final SelectListStatement<T> statement) throws SPersistenceException {
        try {
            final List<T> result = statement.execute(sqlSession);
            if (!cacheEnabled || result.isEmpty() || !PersistentObject.class.isAssignableFrom(result.get(0).getClass())) {
                return result;
            }
            final List<T> newResults = new ArrayList<T>();
            for (final T item : result) {
                newResults.add((T) cache.getOrSave((PersistentObject) item));
            }
            return newResults;
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    public void executeDeleteStatement(final DeleteStatement statement) throws SPersistenceException {
        try {

            if (!cacheEnabled) {
                statement.execute(sqlSession);
            } else {
                // store the statement in statements to execute
                writeStatements.add(statement);
            }
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    public void executeUpdateStatement(final UpdateStatement statement) throws SPersistenceException {
        try {
            if (!cacheEnabled) {
                statement.execute(sqlSession);
            } else {
                // manage cache
                cache.put(statement.getEntity());
                // store the statement in statements to execute
                writeStatements.add(statement);
            }
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    public void executeInsertStatement(final InsertStatement statement) throws SPersistenceException {
        try {
            if (!cacheEnabled) {
                statement.execute(sqlSession);
            } else {
                // manage cache
                cache.put(statement.getEntity());
                // store the statement in statements to execute
                writeStatements.add(statement);
            }
        } catch (final Throwable t) {
            throw new SPersistenceException(t);
        }
    }

    public void close() {
        sqlSession.close();
    }

    public void clearCache() {
        final Collection<MappedStatement> mappedStatements = sqlSession.getConfiguration().getMappedStatements();
        for (final MappedStatement mappedStatement : mappedStatements) {
            final Cache cache = mappedStatement.getCache();
            if (mappedStatement.isUseCache() && cache != null) {
                cache.clear();
            }
        }
    }

    public void commit() {
        sqlSession.commit();
    }

    public void prepare() throws SPersistenceException {
        if (cacheEnabled) {
            for (final WriteStatement writeStatement : writeStatements) {
                writeStatement.execute(sqlSession);
            }
        }
    }

    public void rollback() {
        sqlSession.rollback();
    }

    public ScriptRunner getScriptRunner() {
        final Connection connection = sqlSession.getConnection();
        final ScriptRunner runner = new ScriptRunner(connection);
        return runner;
    }

}
