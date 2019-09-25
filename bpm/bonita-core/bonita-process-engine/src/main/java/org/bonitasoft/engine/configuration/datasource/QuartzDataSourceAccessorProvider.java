package org.bonitasoft.engine.configuration.datasource;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is a hack to let Quartz access datasource beans from SprigContext
 * Quartz support custom connection providers but not non primitive parameters for them
 */
@Component
public class QuartzDataSourceAccessorProvider {

    private static QuartzDataSourceAccessor INSTANCE;

    public QuartzDataSourceAccessorProvider(@Qualifier("bonitaDataSource") DataSource bonitaDataSource,
                                            @Qualifier("bonitaNonXaDataSource") DataSource bonitaNonXaDataSource) {
        INSTANCE = new QuartzDataSourceAccessor(bonitaDataSource, bonitaNonXaDataSource);
    }

    public static QuartzDataSourceAccessor getInstance() {
        return INSTANCE;
    }

}
