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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.io.IOUtil;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingContribution implements BusinessArchiveContribution {

    public static final String FORM_MAPPING_FILE = "form-mapping.xml";

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public String getName() {
        return "FormMapping";
    }

    @Override
    public boolean readFromBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final File file = new File(barFolder, FORM_MAPPING_FILE);
        if (file.exists()) {
            final byte[] content = IOUtil.getContent(file);
            try {
                businessArchive.setFormMappings(new FormMappingModelConverter().deserializeFromXML(content));
            } catch (JAXBException | SAXException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void saveToBarFolder(final BusinessArchive businessArchive, final File barFolder) throws IOException {
        final FormMappingModel formMappingModel = businessArchive.getFormMappingModel();
        try {
            final byte[] fileContent = new FormMappingModelConverter().serializeToXML(formMappingModel);
            final File file = new File(barFolder, FORM_MAPPING_FILE);
            IOUtil.write(file, fileContent);
        } catch (JAXBException | SAXException e) {
            throw new IOException("Cannot write Form Mapping Model to Bar folder", e);
        }
    }
}
