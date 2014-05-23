/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile.xml;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.profile.ExportedProfileBuilder;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;

import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;

/**
 * @author Celine Souchet
 */
public class ProfileBinding extends org.bonitasoft.engine.profile.xml.ProfileBinding {

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        try {
            final String profileName = attributes.get("name");
            if ("Process manager".equals(profileName)) {
                LicenseChecker.getInstance().checkLicenceAndFeature(Features.WEB_PROFILE_PO);
            }
            profileBuilder = new ExportedProfileBuilder(profileName, Boolean.valueOf(attributes.get("isDefault")));
        } catch (final IllegalStateException e) {
            // Nothing to do
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (profileBuilder != null) {
            if ("description".equals(name)) {
                profileBuilder.setDescription(value);
            }
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (profileBuilder != null) {
            if ("profileEntries".equals(name)) {
                profileBuilder.setParentProfileEntries((List<ExportedParentProfileEntry>) value);
            } else if ("profileMapping".equals(name)) {
                profileBuilder.setProfileMapping((ExportedProfileMapping) value);
            }
        }
    }

    @Override
    public ExportedProfile getObject() {
        if (profileBuilder != null) {
            return profileBuilder.done();
        }
        return null;
    }

}
