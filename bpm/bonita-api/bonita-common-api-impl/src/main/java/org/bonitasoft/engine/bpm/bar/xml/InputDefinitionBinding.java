package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.io.xml.XMLParseException;

/**
 * @author Matthieu Chaffotte
 */
public class InputDefinitionBinding extends NamedElementBinding {

    private String type;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        type = attributes.get(XMLProcessDefinition.TYPE);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) throws XMLParseException {
    }

    @Override
    public void setChildObject(final String name, final Object value) throws XMLParseException {
    }

    @Override
    public Object getObject() {
        return new InputDefinitionImpl(name, type, description);
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CONTRACT_INPUT_NODE;
    }

}
