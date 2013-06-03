package org.bonitasoft.engine.core.migration.model;

import java.io.Serializable;
import java.util.List;

public interface SMigrationMapping extends Serializable {

    String getSourceName();

    int getSourceState();// breakpoint will be before this state

    String getTargetName();

    int getTargetState();

    List<SConnectorDefinitionWithEnablement> getConnectors();

    List<SOperationWithEnablement> getOperations();
}
