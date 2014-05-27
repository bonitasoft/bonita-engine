package com.bonitasoft.engine.expression;

import com.bonitasoft.engine.business.data.BusinessDataRepository;

public class DummyServerDAO {

    private final BusinessDataRepository businessDataRepository;

    public DummyServerDAO(final BusinessDataRepository businessDataRepository) {
        this.businessDataRepository = businessDataRepository;
    }

    public BusinessDataRepository getBusinessDataRepository() {
        return businessDataRepository;
    }
}
