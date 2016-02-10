/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.tcp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MethodCall implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Map<String, Serializable> options;
    private final String apiInterfaceName;
    private final String methodName;
    private final List<String> classNameParameters;
    private final Object[] parametersValues;

    public MethodCall(Map<String, Serializable> options, String apiInterfaceName, String methodName, List<String> classNameParameters, Object[] parametersValues) {
        this.options = options;
        this.apiInterfaceName = apiInterfaceName;
        this.methodName = methodName;
        this.classNameParameters = classNameParameters;
        this.parametersValues = parametersValues;
    }

    public Map<String, Serializable> getOptions() {
        return options;
    }

    public String getApiInterfaceName() {
        return apiInterfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getClassNameParameters() {
        return classNameParameters;
    }

    public Object[] getParametersValues() {
        return parametersValues;
    }

    @Override
    public String toString() {
        return "MethodCall [options=" + options + ", apiInterfaceName="
                + apiInterfaceName + ", methodName=" + methodName
                + ", classNameParameters=" + classNameParameters
                + ", parametersValues=" + Arrays.toString(parametersValues)
                + "]";
    }

    
}
