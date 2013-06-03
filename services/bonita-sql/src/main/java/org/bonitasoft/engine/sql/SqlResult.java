package org.bonitasoft.engine.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlResult {

    private final ResultSet resultSet;

    private final Statement statement;

    public SqlResult(final Statement statement, final ResultSet resultSet) {
        this.resultSet = resultSet;
        this.statement = statement;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void close() throws SqlSessionException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }
}
