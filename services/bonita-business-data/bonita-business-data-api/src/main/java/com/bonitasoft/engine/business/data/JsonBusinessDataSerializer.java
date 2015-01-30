/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.io.IOException;
import java.util.List;

import com.bonitasoft.engine.bdm.Entity;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface JsonBusinessDataSerializer {

    String EMPTY_OBJECT = "{}";

    String serializeEntity(Entity entity, String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException;

    String serializeEntity(List<Entity> childEntity, String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException;

}
