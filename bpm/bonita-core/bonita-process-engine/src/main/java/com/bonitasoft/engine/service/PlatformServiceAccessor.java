/*
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.service;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Zhao Na
 */
public interface PlatformServiceAccessor extends org.bonitasoft.engine.service.PlatformServiceAccessor {

    SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor();

}
