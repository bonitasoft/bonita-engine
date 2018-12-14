/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.bpm.bar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;

/**
 * @author mazourd
 */
public class ActorMappingMarshaller {

    private static final String XSD_MODEL = "/actorMapping.xsd";

    public ActorMappingMarshaller() {
    }

    public ActorMapping deserializeFromXML(final byte[] xmlModel) throws XmlMarshallException {
        return unmarshall(xmlModel);
    }

    private ActorMapping unmarshall(final byte[] model) throws XmlMarshallException {
        if (model == null) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(model)) {
            final JAXBContext contextObj = JAXBContext.newInstance(ActorMapping.class);
            final Unmarshaller um = contextObj.createUnmarshaller();
            final StreamSource ss = new StreamSource(bais);
            final JAXBElement<ActorMapping> jaxbElement = um.unmarshal(ss, ActorMapping.class);
            return jaxbElement.getValue();
        } catch (JAXBException | IOException e) {
            throw new XmlMarshallException("Failed to deserialize the ActorMapping", e);
        }
    }

    public byte[] serializeToXML(final ActorMapping model) throws XmlMarshallException {
        return marshall(model);
    }

    private byte[] marshall(final ActorMapping model) throws XmlMarshallException {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final JAXBContext contextObj = JAXBContext.newInstance(model.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(model, baos);
            return baos.toByteArray();
        } catch (JAXBException | IOException e) {
            throw new XmlMarshallException("Failed to serialize the ActorMapping", e);
        }
    }

}
