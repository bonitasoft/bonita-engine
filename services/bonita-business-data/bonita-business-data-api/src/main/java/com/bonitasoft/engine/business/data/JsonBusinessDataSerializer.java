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
