package org.bonitasoft.engine.core.process.document.model.builder.impl;

import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilders;

public class SProcessDocumentBuildersImpl implements SProcessDocumentBuilders {

    @Override
    public SProcessDocumentBuilder getSProcessDocumentBuilder() {
        return new SProcessDocumentBuilderImpl();
    }

}
