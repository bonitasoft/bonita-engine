package org.bonitasoft.engine.configuration.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

/**
 * This is a hack to let Quartz access datasource beans from SprigContext
 * Quartz support custom connection providers but not non primitive parameters for them
 */
public class QuartzConnectionProvider implements ConnectionProvider {

    private Boolean isXaDataSource;
    private DataSource dataSource;

    /**
     * this is called by quartz (given by the configuration)
     *
     * @param isXaDataSource true if the datasource returned by this configuration provider should be the xa datasource
     */
    public void setXaDataSource(boolean isXaDataSource) {
        this.isXaDataSource = isXaDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException {

    }

    @Override
    public void initialize() throws SQLException {
        if (isXaDataSource == null) {
            throw new IllegalStateException("Quartz datasource is not set");
        }
        if (isXaDataSource) {
            dataSource = QuartzDataSourceAccessorProvider.getInstance().getBonitaDataSource();
        } else {
            dataSource = QuartzDataSourceAccessorProvider.getInstance().getBonitaNonXaDataSource();
        }
    }
}
