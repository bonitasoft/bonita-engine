/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl;

import com.bonitasoft.engine.api.impl.converter.ApplicationModelConverterExt;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.api.impl.application.ApplicationAPIDelegate;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIExt extends org.bonitasoft.engine.api.impl.ApplicationAPIImpl {

    @Override
    protected ApplicationAPIDelegate getApplicationAPIDelegate() {
        return new ApplicationAPIDelegate(getTenantAccessor(), new ApplicationModelConverterExt(getTenantAccessor().getPageService()),
                SessionInfos.getUserIdFromSession());
    }
}
