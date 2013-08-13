/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Charles Souillard
 * @author Frederic Bouquet
 */
public class MybatisSqlSessionFactoryProvider {

    private SqlSessionFactory sqlSessionFactory;

    private final String configuration;

    private final List<AbstractMyBatisConfigurationsProvider> myBatisConfigurationsProviders;

    private final Properties properties;

    public MybatisSqlSessionFactoryProvider(final String configuration, final List<AbstractMyBatisConfigurationsProvider> myBatisConfigurationsProviders,
            final Properties properties) {
        this.configuration = configuration;
        this.myBatisConfigurationsProviders = myBatisConfigurationsProviders;
        this.properties = properties;
    }

    public MybatisSqlSessionFactoryProvider(final String configuration, final List<AbstractMyBatisConfigurationsProvider> myBatisConfigurationsProviders) {
        this.configuration = configuration;
        this.myBatisConfigurationsProviders = myBatisConfigurationsProviders;
        properties = null;
    }

    public SqlSessionFactory getSqlSessionFactory() throws SPersistenceException {
        if (sqlSessionFactory == null) {
            Reader reader;
            try {
                URL url;
                String fileContent;

                try {
                    url = new URL(configuration);
                } catch (MalformedURLException e) {
                    url = this.getClass().getResource(configuration);

                    if (url == null) {
                        throw new IOException("configuration file not found, path=" + configuration);
                    }

                }
                // replace variable of the mybatis.config.xml file with properties given to the MyBatisSqlSessionFactoryProvider
                fileContent = new String(IOUtil.getAllContentFrom(url));
                if (properties != null && !properties.isEmpty()) {
                    final Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z\\.0-9_]+)\\}");
                    final Matcher matcher = pattern.matcher(fileContent);
                    final StringBuffer sb = new StringBuffer();
                    while (matcher.find()) {

                        final String key = matcher.group(1);
                        if (!properties.containsKey(key)) {
                            throw new RuntimeException("Missing property: " + key);
                        }
                        String property = properties.getProperty(key);
                        property = property.replaceAll("\\\\", "/");
                        matcher.appendReplacement(sb, property);
                    }
                    matcher.appendTail(sb);
                    fileContent = sb.toString();
                }

                reader = new StringReader(fileContent);
            } catch (final IOException e) {
                throw new SPersistenceException(e);
            }

            final XMLConfigBuilder parser = new XMLConfigBuilder(reader, null, null);
            final Configuration config = parser.parse();
            // get mappings and aliases from other files
            for (final AbstractMyBatisConfigurationsProvider myBatisConfigurationsProvider : myBatisConfigurationsProviders) {
                for (final AbstractMyBatisConfiguration mapperAndAliases : myBatisConfigurationsProvider.getConfigurations()) {

                    for (final Entry<String, String> alias : mapperAndAliases.getTypeAliases().entrySet()) {
                        config.getTypeAliasRegistry().registerAlias(alias.getKey(), alias.getValue());
                    }
                }
            }
            for (final AbstractMyBatisConfigurationsProvider myBatisConfigurationsProvider : myBatisConfigurationsProviders) {
                for (final AbstractMyBatisConfiguration mapperAndAliases : myBatisConfigurationsProvider.getConfigurations()) {
                    for (String mapper : mapperAndAliases.getMappers()) {
                        mapper = mapper.trim();
                        InputStream inputStream;
                        try {
                            inputStream = Resources.getResourceAsStream(mapper);
                        } catch (final IOException e) {
                            throw new SPersistenceException(e);
                        }
                        final XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, config, mapper, config.getSqlFragments());
                        mapperParser.parse();
                    }
                }
            }
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
        }
        return sqlSessionFactory;
    }

}
