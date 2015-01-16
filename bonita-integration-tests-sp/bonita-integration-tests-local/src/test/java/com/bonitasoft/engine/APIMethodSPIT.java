/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.test.APIMethodIT;
import org.junit.Test;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.impl.LogAPIExt;
import com.bonitasoft.engine.api.impl.MonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.NodeAPIImpl;
import com.bonitasoft.engine.api.impl.PageAPIExt;
import com.bonitasoft.engine.api.impl.PlatformMonitoringAPIImpl;
import com.bonitasoft.engine.api.impl.ProcessAPIExt;
import com.bonitasoft.engine.api.impl.ReportingAPIExt;

@SuppressWarnings("javadoc")
public class APIMethodSPIT extends APIMethodIT {

    @Override
    @Test
    public void checkAllMethodsOfProcessAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ProcessAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfLogAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(LogAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfMonitoringAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(MonitoringAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfPlatformMonitoringAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(PlatformMonitoringAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfReportAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ReportingAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfNodeAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(NodeAPIImpl.class);
    }

    @Test
    public void checkAllMethodsOfPageAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(PageAPIExt.class);
    }

    @Test
    public void checkAllMethodsOfApplicationAPIContainsSerializableParameters() {
        checkAllParametersAreSerializable(ApplicationAPI.class);
    }

}
