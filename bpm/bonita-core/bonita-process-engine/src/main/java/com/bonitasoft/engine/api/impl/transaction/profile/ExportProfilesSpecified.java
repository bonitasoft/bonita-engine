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
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.xml.XMLNode;
import org.bonitasoft.engine.xml.XMLWriter;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ExportProfilesSpecified extends AbstractExportProfiles {

    private final List<Long> profileIds;

    public ExportProfilesSpecified(final ProfileService profileService, final IdentityService identityService, final XMLWriter writer,
            final List<Long> profileIds) {
        super(profileService, identityService, writer);
        this.profileIds = profileIds;
    }

    @Override
    protected XMLNode getProfilesXmlNode() throws SBonitaException {
        final StringBuilder stringBuilderNode = new StringBuilder();
        stringBuilderNode.append(PROFILES_NAMESPACE_PREFIX);
        stringBuilderNode.append(":");
        stringBuilderNode.append(PROFILES_TAG_NAME);
        final XMLNode profilesNode = new XMLNode(stringBuilderNode.toString());
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("xmlns:");
        stringBuilder.append(PROFILES_NAMESPACE_PREFIX);
        profilesNode.addAttribute(stringBuilder.toString(), PROFILES_NAMESPACE);

        final List<SProfile> sProfiles = getProfiles();
        for (final SProfile sProfile : sProfiles) {
            profilesNode.addChild(getProfileXmlNode(sProfile));
        }
        return profilesNode;
    }

    private List<SProfile> getProfiles() throws SProfileNotFoundException {
        final List<SProfile> sProfiles = getProfileService().getProfiles(profileIds);
        return sProfiles;
    }

}
