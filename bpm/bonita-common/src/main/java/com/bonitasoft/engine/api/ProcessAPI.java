/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.api.DebugAPI;
import org.bonitasoft.engine.api.DocumentAPI;

/**
 * @author Matthieu Chaffotte
 */
public interface ProcessAPI extends org.bonitasoft.engine.api.ProcessAPI, ProcessManagementAPI, ProcessRuntimeAPI, DocumentAPI, DebugAPI {

}
