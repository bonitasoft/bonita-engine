/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 */
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.form.FormMapping;

/**
 * author Emmanuel Duchastenier
 */
public interface ProcessConfigurationAPI extends org.bonitasoft.engine.api.ProcessConfigurationAPI {

    /**
     * Update a form mapping with the given values
     *
     * @param formMappingId
     *        the form mapping to update
     * @param url
     *        the name of the form or the url to the form
     * @param pageId
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the formMappingId is not an existing form mapping
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         when there is an issue when updating the form mapping
     * @since 7.0.0
     */
    FormMapping updateFormMapping(final long formMappingId, final String url, Long pageId) throws FormMappingNotFoundException, UpdateException;

    /**
     * Updates a groovy expression content at runtime, for all instances of a given process definition. Note that no check is done on the new content of the
     * expression, no new dependency can be added, the return type will remain unchanged.
     * 
     * @param processDefintionId the ID of the process on which to change a groovy expression
     * @param expressionDefinitionId the ID of the expression to update
     * @param content the content to update
     * @throws NotFoundException if the process or the expression is not found for the given ID
     */
    void updateGroovyExpression(long processDefintionId, long expressionDefinitionId, String content) throws NotFoundException, UpdateException;
}
