/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.converter;

import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationField;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.page.PageService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationModelConverterExt extends ApplicationModelConverter {

    public ApplicationModelConverterExt(final PageService pageService) {
        super(pageService);
    }

    @Override
    protected Long getLayoutId(final ApplicationCreator creator) throws CreationException {
        Long layoutId = (Long) creator.getFields().get(ApplicationField.LAYOUT_ID);
        if (layoutId == null) {
            return super.getLayoutId(creator);
        }
        return layoutId;
    }
}
