package com.bonitasoft.engine.business.data.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;

/**
 * @author Romain Bioteau
 *
 */
public class SchemaGenerator {

	private Configuration cfg;
	private Dialect dialect;

	public SchemaGenerator(Dialect dialect,Properties properties, List<String> classNameList) throws SBusinessDataRepositoryDeploymentException{
		this.cfg = new Configuration();
		this.cfg.setProperties(properties);
		this.cfg.setProperty("hibernate.hbm2ddl.auto","update");
		this.cfg.setProperty("hibernate.current_session_context_class","jta");
		this.cfg.setProperty("hibernate.transaction.factory_class","org.hibernate.transaction.JTATransactionFactory");
		this.cfg.setProperty("hibernate.transaction.manager_lookup_class","org.hibernate.transaction.BTMTransactionManagerLookup");
		this.cfg.setProperty("hibernate.connection.datasource",properties.getProperty("hibernate.connection.datasource"));
		this.dialect = dialect;
		this.cfg.setProperty("hibernate.dialect", dialect.getClass().getName());
		for(String className : classNameList){
			Class<?> annotatedClass;
			try {
				annotatedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
				cfg.addAnnotatedClass(annotatedClass);
			} catch (ClassNotFoundException e) {
				throw new SBusinessDataRepositoryDeploymentException(e);
			}

		}

	}

	/**
	 * Method that actually creates the file.  
	 * @param dbDialect to use
	 * @throws SQLException 
	 */
	public String[] generate() throws SQLException{
		final ConnectionProvider connectionProvider = ConnectionProviderFactory.newConnectionProvider(cfg.getProperties());
		final Connection connection = connectionProvider.getConnection();
		final DatabaseMetadata databaseMetadata = new DatabaseMetadata(connection, dialect);
		return cfg.generateSchemaUpdateScript(dialect,databaseMetadata);
	}

}