package org.bonitasoft.engine.sql.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.bonitasoft.engine.sql.Converter;
import org.bonitasoft.engine.sql.DefaultSqlConverter;
import org.bonitasoft.engine.sql.SqlResult;
import org.bonitasoft.engine.sql.SqlSession;
import org.bonitasoft.engine.sql.SqlSessionException;
import org.bonitasoft.engine.sql.SqlSessionManager;
import org.bonitasoft.engine.sql.test.model.DataType;
import org.bonitasoft.engine.sql.test.model.DataTypeOther;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class SqlTest {

    public SqlTest() throws ConfigurationException {
    }

    @Rule
    public TestName testName = new TestName();

    private static Properties props;

    static {
        props = new Properties();
        final InputStream in = SqlTest.class.getResourceAsStream("/org/bonitasoft/engine/sql/test/" + getDatabase() + ".properties");
        try {
            props.load(in);
            // System.getProperty(key)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDatabase() {
        return System.getProperty("sysprop.bonita.db.vendor", "h2");
    }

    @Test
    public void testSelect() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            String query = getCreateTableDatatypeString();
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (1, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "select * from datatype where id=1";
            final SqlResult sqlResult = sqlSession.select(query);
            final ResultSet resultSet = sqlResult.getResultSet();
            resultSet.next();
            assertNotNull(resultSet);

            query = "DROP TABLE datatype;";
            sqlSession.update(query);
        } finally {
            sqlSession.disconnect();
        }
    }

    @Test
    public void testSelectByQueryParameters() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            String query = getCreateTableDatatypeString();
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (1, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "select * from datatype where id=? and col_int=? and col_boolean=? and col_double=?";
            final Object[] queryParameters = new Object[4];
            queryParameters[0] = 1;
            queryParameters[1] = 1;
            queryParameters[2] = 1;
            queryParameters[3] = 1.7;
            final SqlResult sqlResult = sqlSession.select(query, queryParameters);
            final ResultSet resultSet = sqlResult.getResultSet();
            resultSet.next();
            assertNotNull(resultSet);
            assertEquals(1, resultSet.getInt(1));
            assertEquals(1, resultSet.getInt(2));
            assertTrue(resultSet.getBoolean(3));
            assertEquals("2008-12-15", resultSet.getDate(6).toString());
            assertEquals("2010-11-09 23:01:35.0", resultSet.getTimestamp(7).toString());
            assertEquals("10:15:57", resultSet.getTime(8).toString());
            sqlResult.close();

            query = "DROP TABLE datatype;";
            sqlSession.update(query);
        } finally {
            sqlSession.disconnect();
        }
    }

    @Test
    public void testUpdate() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            String query = getCreateTableDatatypeString();
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (1, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "select * from datatype where id = 1";
            SqlResult sqlResult = sqlSession.select(query);
            ResultSet rs = sqlResult.getResultSet();
            rs.next();
            assertEquals(1, rs.getInt(1));
            assertEquals(1, rs.getInt(2));
            assertTrue(rs.getBoolean(3));
            assertEquals("2008-12-15", rs.getDate(6).toString());
            assertEquals("2010-11-09 23:01:35.0", rs.getTimestamp(7).toString());
            assertEquals("10:15:57", rs.getTime(8).toString());

            query = "UPDATE datatype SET col_int = null, col_boolean = null, col_double = null, col_decimal = null, col_date = null, col_timestamp = null, col_time = null, col_char = null, col_varchar = null, col_binary = null, col_blob = null WHERE id = 1;";
            sqlSession.update(query);

            query = "select * from datatype where id = 1";
            sqlResult = sqlSession.select(query);
            rs = sqlResult.getResultSet();
            rs.next();
            assertEquals(0, rs.getInt(2));
            assertTrue(!rs.getBoolean(3));
            assertEquals(0, 0, rs.getDouble(4));
            assertNull(rs.getBigDecimal(5));
            assertNull(rs.getDate(6));
            assertNull(rs.getTime(8));

            query = "DELETE FROM datatype WHERE id = 1";
            sqlSession.update(query);
        } finally {
            final String query = "DROP TABLE datatype;";
            sqlSession.update(query);
            sqlSession.disconnect();
        }
    }

    @Test
    public void testExecuteSelect() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            String query = getCreateTableDatatypeString();
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (1, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (2, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "select * from datatype";
            final Map<String, String> columnFieldMap = new HashMap<String, String>();
            columnFieldMap.put("id", "id");
            columnFieldMap.put("col_int", "intCol");
            columnFieldMap.put("col_boolean", "booleanCol");
            columnFieldMap.put("col_decimal", "decimalCol");
            columnFieldMap.put("col_double", "doubleCol");
            columnFieldMap.put("col_time", "timeCol");
            columnFieldMap.put("col_date", "dateCol");
            columnFieldMap.put("col_timestamp", "datetimeCol");
            columnFieldMap.put("col_binary", "binaryCol");
            columnFieldMap.put("col_varchar", "varcharCol");
            columnFieldMap.put("col_char", "charCol");
            columnFieldMap.put("col_blob", "blobCol");
            columnFieldMap.put("col_subdatatypeid", "subDateType");

            final Map<String, Class<?>> sqlTypeJavaClassMap = new HashMap<String, Class<?>>();
            sqlTypeJavaClassMap.put("INT", Integer.class);
            sqlTypeJavaClassMap.put("BOOLEAN", Boolean.class);
            sqlTypeJavaClassMap.put("DECIMAL", BigDecimal.class);
            sqlTypeJavaClassMap.put("DOUBLE", Double.class);
            sqlTypeJavaClassMap.put("TIME", java.util.Date.class);
            sqlTypeJavaClassMap.put("DATE", java.util.Date.class);
            sqlTypeJavaClassMap.put("DATETIME", java.util.Date.class);
            sqlTypeJavaClassMap.put("VARCHAR", String.class);
            sqlTypeJavaClassMap.put("CHAR", String.class);
            sqlTypeJavaClassMap.put("BLOB", byte.class);

            final List<DataType> list = sqlSession.executeSelect(DataType.class, query, columnFieldMap, sqlTypeJavaClassMap);
            assertEquals(2, list.size());

            final List<DataType> list1 = sqlSession.executeSelect(DataType.class, query, null, null);
            assertEquals(2, list1.size());
        } finally {
            final String query = "DROP TABLE `datatype`;";
            sqlSession.update(query);
            sqlSession.disconnect();
        }
    }

    @Test
    public void testExecuteSelectWithFieldDescriptor() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            String query = getCreateTableDatatypeString();
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (1, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "INSERT INTO datatype VALUES (2, 1, 1, 1.7, 6.1, '2008-12-15', '2010-11-09 23:01:35', '10:15:57', '', '', null, null, 1);";
            sqlSession.update(query);

            query = "select * from datatype";
            final Map<String, Converter> columnMappings = new HashMap<String, Converter>();
            columnMappings.put("col_varchar", new DefaultSqlConverter());
            columnMappings.put("col_int", new DefaultSqlConverter());
            columnMappings.put("col_boolean", new DefaultSqlConverter());
            columnMappings.put("col_decimal", new DefaultSqlConverter());
            columnMappings.put("col_char", new DefaultSqlConverter());
            columnMappings.put("col_time", new DefaultSqlConverter());

            final Map<String, String> columnFieldMap = new HashMap<String, String>();
            columnFieldMap.put("id", "id");
            columnFieldMap.put("col_int", "intCol");
            columnFieldMap.put("col_boolean", "booleanCol");
            columnFieldMap.put("col_decimal", "decimalCol");
            columnFieldMap.put("col_double", "doubleCol");
            columnFieldMap.put("col_time", "timeCol");
            columnFieldMap.put("col_date", "dateCol");
            columnFieldMap.put("col_timestamp", "datetimeCol");
            columnFieldMap.put("col_binary", "binaryCol");
            columnFieldMap.put("col_varchar", "varcharCol");
            columnFieldMap.put("col_char", "charCol");
            columnFieldMap.put("col_blob", "blobCol");
            columnFieldMap.put("col_subdatatypeid", "subDateType");

            final List<DataTypeOther> list = sqlSession.executeSelectWithConverters(DataTypeOther.class, query, columnFieldMap, columnMappings);

            assertEquals(2, list.size());
        } finally {
            final String query = "DROP TABLE `datatype`;";
            sqlSession.update(query);
            sqlSession.disconnect();
        }
    }

    @Test(expected = SqlSessionException.class)
    public void testFailing() throws Exception {
        final SqlSession sqlSession = SqlSessionManager.getSession(getUrl(), getDriver(), getUser(), getPassword());

        try {
            final String query = "create " + getCreateTableDatatypeString();
            sqlSession.update(query); // Wrong SQL, throw
                                      // SqlDataSourceException
        } finally {
            sqlSession.disconnect();
        }
    }

    private String getUrl() throws IOException {
        if (System.getProperty("jdbc.url") != null) {
            return System.getProperty("jdbc.url");
        }
        return props.getProperty("jdbc.url");
    }

    private String getDriver() throws IOException {
        if (System.getProperty("jdbc.driverClass") != null) {
            return System.getProperty("jdbc.driverClass");
        }
        return props.getProperty("jdbc.driverClass");
    }

    private String getUser() throws IOException {
        if (System.getProperty("jdbc.user") != null) {
            return System.getProperty("jdbc.user");
        }
        return props.getProperty("jdbc.user");
    }

    private String getPassword() throws IOException {
        if (System.getProperty("jdbc.password") != null) {
            return System.getProperty("jdbc.password");
        }
        return props.getProperty("jdbc.password");
    }

    private String getCreateTableDatatypeString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CREATE TABLE  datatype (");
        stringBuilder.append("id int NOT NULL,");
        stringBuilder.append("col_int int,");
        stringBuilder.append("col_boolean BOOLEAN,");
        stringBuilder.append("col_double DOUBLE,");
        stringBuilder.append("col_decimal DECIMAL,");
        stringBuilder.append("col_date DATE,");
        stringBuilder.append("col_timestamp TIMESTAMP,");
        stringBuilder.append("col_time TIME,");
        stringBuilder.append("col_char CHAR(5),");
        stringBuilder.append("col_varchar VARCHAR(10),");
        stringBuilder.append("col_binary BINARY(255),");
        stringBuilder.append("col_blob LONGBLOB,");
        stringBuilder.append("col_subdatatypeid int,");
        stringBuilder.append("PRIMARY KEY (`id`));");
        return stringBuilder.toString();
    }

}
