/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public class ProfileMemberUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProfileMemberField {
        GROUP_ID, ROLE_ID, USER_ID, DISPLAY_NAME_PART_1, DISPLAY_NAME_PART_2, DISPLAY_NAME_PART_3;
    }

    private final Map<ProfileMemberField, Serializable> fields;

    public ProfileMemberUpdater() {
        fields = new HashMap<ProfileMemberField, Serializable>(3);
    }

    public void groupId(final long groupId) {
        fields.put(ProfileMemberField.GROUP_ID, groupId);
    }

    public void roleId(final long roleId) {
        fields.put(ProfileMemberField.ROLE_ID, roleId);
    }

    public void userId(final long userId) {
        fields.put(ProfileMemberField.USER_ID, userId);
    }

    public void displayNamePart1(final String displayNamePart1) {
        fields.put(ProfileMemberField.DISPLAY_NAME_PART_1, displayNamePart1);
    }

    public void displayNamePart2(final String displayNamePart2) {
        fields.put(ProfileMemberField.DISPLAY_NAME_PART_2, displayNamePart2);
    }

    public void displayNamePart3(final String displayNamePart3) {
        fields.put(ProfileMemberField.DISPLAY_NAME_PART_3, displayNamePart3);
    }

    public Map<ProfileMemberField, Serializable> getFields() {
        return fields;
    }

}
