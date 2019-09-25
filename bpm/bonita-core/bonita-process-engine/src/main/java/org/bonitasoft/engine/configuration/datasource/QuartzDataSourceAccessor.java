package org.bonitasoft.engine.configuration.datasource;


import javax.sql.DataSource;

/**
 * This is a hack to let Quartz access datasource beans from SprigContext
 * Quartz support custom connection providers but not non primitive parameters for them
 */
public class QuartzDataSourceAccessor {

    private DataSource bonitaDataSource;
    private DataSource bonitaNonXaDataSource;

    public QuartzDataSourceAccessor(DataSource bonitaDataSource, DataSource bonitaNonXaDataSource) {
        this.bonitaDataSource = bonitaDataSource;
        this.bonitaNonXaDataSource = bonitaNonXaDataSource;
    }

    public DataSource getBonitaDataSource() {
        return bonitaDataSource;
    }

    public DataSource getBonitaNonXaDataSource() {
        return bonitaNonXaDataSource;
    }
}
