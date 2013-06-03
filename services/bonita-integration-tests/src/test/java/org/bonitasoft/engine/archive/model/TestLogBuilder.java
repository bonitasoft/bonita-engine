package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestLogBuilder extends CRUDELogBuilder implements SPersistenceLogBuilder, HasCRUDEAction {

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(0, objectId);
        return this;
    }

    @Override
    public String getObjectIdKey() {
        return "numericIndex1";
    }

    @Override
    protected String getActionTypePrefix() {
        return "TEST";
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        // TODO Auto-generated method stub
    }

}
