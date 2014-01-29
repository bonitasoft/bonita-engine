/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 16:44
 */
public interface ReportDeployer {

    void deploy(String name, String description, byte[] screenShot, byte[] content) throws Exception;
}
