/**
 * Copyright (C) 2011  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.bonitasoft.engine.test;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLogSeverity;
import org.bonitasoft.engine.services.BusinessLoggerServiceConfiguration;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class MockBusinessLoggerServiceConfigurationImpl implements
    BusinessLoggerServiceConfiguration {
  private List<String> loggable;
  
  public MockBusinessLoggerServiceConfigurationImpl() {
    loggable = new ArrayList<String>();
    loggable.add("execute_connector_:" + SBusinessLogSeverity.BUSINESS);
    loggable.add("variable_update_:" + SBusinessLogSeverity.BUSINESS);
    loggable.add("execute_connector_:" + SBusinessLogSeverity.INTERNAL);
    loggable.add("variable_update_:" + SBusinessLogSeverity.INTERNAL);
  }
  
  public boolean needsInferCaller() {
    final String strNeedsToInfer = System.getProperty("org.bonitasoft.needstoinfercaller");
    boolean needsToInfer = true;
    if(strNeedsToInfer != null){
      needsToInfer = Boolean.parseBoolean(strNeedsToInfer);
    }
    return needsToInfer;
  }

  public boolean isLogable(String actionType, SBusinessLogSeverity severity) {
    return !loggable.contains(actionType + ":" + severity.toString());
  }

}

