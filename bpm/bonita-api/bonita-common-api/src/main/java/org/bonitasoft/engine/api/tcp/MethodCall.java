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
