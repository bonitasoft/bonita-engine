/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Baptiste Mesta
 */
public class PlatformMyBatisConfiguration extends AbstractMyBatisConfiguration {

    public PlatformMyBatisConfiguration(final Map<String, String> typeAliases, final List<String> mappers, final Map<String, String> classAliasMappings,
            final Map<String, String> classFieldAliasMappings, final Set<StatementMapping> statementMapping, final Map<String, String> dbStatementsMapping,
            final Map<String, String> entityMappings) {
        super(typeAliases, mappers, classAliasMappings, classFieldAliasMappings, statementMapping, dbStatementsMapping, entityMappings);
    }
}
