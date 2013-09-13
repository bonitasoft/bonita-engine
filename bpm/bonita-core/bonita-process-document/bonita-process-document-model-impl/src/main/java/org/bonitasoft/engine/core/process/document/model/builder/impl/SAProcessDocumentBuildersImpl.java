package org.bonitasoft.engine.core.process.document.model.builder.impl;

import org.bonitasoft.engine.core.process.document.model.builder.SAProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.builder.SAProcessDocumentBuilders;

public class SAProcessDocumentBuildersImpl implements SAProcessDocumentBuilders {

    @Override
    public SAProcessDocumentBuilder getSAProcessDocumentBuilder() {
        return new SAProcessDocumentBuilderImpl();
    }

}
