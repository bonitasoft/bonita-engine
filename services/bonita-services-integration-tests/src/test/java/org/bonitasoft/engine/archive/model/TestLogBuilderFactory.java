package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestLogBuilderFactory extends CRUDELogBuilderFactory implements SPersistenceLogBuilderFactory {

    @Override
    public TestLogBuilder createNewInstance() {
        return new TestLogBuilder();
    }

    @Override
    public String getObjectIdKey() {
        return "numericIndex1";
    }

}
