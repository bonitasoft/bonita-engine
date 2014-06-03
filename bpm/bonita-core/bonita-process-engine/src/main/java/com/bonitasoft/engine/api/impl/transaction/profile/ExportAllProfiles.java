/*******************************************************************************
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.xml.XMLNode;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ExportAllProfiles extends AbstractExportProfiles {

    public ExportAllProfiles(final ProfileService profileService, final IdentityService identityService, final XMLWriter writer) {
        super(profileService, identityService, writer);
    }

    @Override
    protected XMLNode getProfilesXmlNode() throws SBonitaException {
        final StringBuilder stringBuilderNodeName = new StringBuilder();
        stringBuilderNodeName.append(PROFILES_NAMESPACE_PREFIX);
        stringBuilderNodeName.append(":");
        stringBuilderNodeName.append(PROFILES_TAG_NAME);
        final XMLNode profilesNode = new XMLNode(stringBuilderNodeName.toString());
        final StringBuilder stringBuilderPrefix = new StringBuilder();
        stringBuilderPrefix.append("xmlns:");
        stringBuilderPrefix.append(PROFILES_NAMESPACE_PREFIX);
        profilesNode.addAttribute(stringBuilderPrefix.toString(), PROFILES_NAMESPACE);

        int index = 0;
        List<SProfile> sProfiles = searchProfiles(index);
        while (sProfiles.size() > 0) {
            for (final SProfile sProfile : sProfiles) {
                profilesNode.addChild(getProfileXmlNode(sProfile));
            }
            index++;
            sProfiles = searchProfiles(index);
        }

        return profilesNode;
    }

}
