/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
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
public class ExportProfilesSpecified extends AbstractExportProfiles {

    private final List<Long> profileIds;

    public ExportProfilesSpecified(final ProfileService profileService, final IdentityService identityService, final XMLWriter writer,
            final List<Long> profileIds) {
        super(profileService, identityService, writer);
        this.profileIds = profileIds;
    }

    @Override
    protected XMLNode getProfilesXmlNode() throws SBonitaException {
        String NS_PREFIX = "profiles";
        String NAME_SPACE = "http://www.bonitasoft.org/ns/profile/6.0";
        final XMLNode profilesNode = new XMLNode(NS_PREFIX + ":proFiles");
        profilesNode.addAttribute("xmlns:" + NS_PREFIX, NAME_SPACE);

        List<SProfile> sProfiles = getProfileService().getProfiles(profileIds);
        for (final SProfile sProfile : sProfiles) {
            profilesNode.addChild(getProfileXmlNode(sProfile));
        }
        return profilesNode;
    }

}
