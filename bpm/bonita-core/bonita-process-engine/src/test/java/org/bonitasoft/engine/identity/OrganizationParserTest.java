/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedGroup;
import org.bonitasoft.engine.identity.xml.ExportedRole;
import org.bonitasoft.engine.identity.xml.ExportedUser;
import org.bonitasoft.engine.identity.xml.ExportedUserMembership;
import org.bonitasoft.engine.identity.xml.Organization;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class OrganizationParserTest {

    private OrganizationParser organizationParser = new OrganizationParser();


    @Test
    public void should_parse_organization_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/complexOrganization.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
        assertThat(organization.getUsers()).hasSize(3);
        assertThat(organization.getRoles()).hasSize(2);
        assertThat(organization.getGroups()).hasSize(4);
        assertThat(organization.getMemberships()).hasSize(2);
    }

    @Test
    public void should_replace_old_namespace() throws Exception {
        //given
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<organization:Organization xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema\">\n" +
                "\t<users>\n" +
                "\t</users>\n" +
                "\t<roles>\n" +
                "\t</roles>\n" +
                "\t<groups>\n" +
                "\t</groups>\n" +
                "\t<memberships>\n" +
                "\t</memberships>\n" +
                "</organization:Organization>";
        //when
        String newOrganizationContent = organizationParser.convert(organizationParser.convert(content));
        //then
        assertThat(newOrganizationContent).contains("http://documentation.bonitasoft.com/organization-xml-schema/1.1");
    }

    @Test
    public void should_parse_simpleOrganization_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/simpleOrganization.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
    }

    @Test
    public void should_parse_organization_with_both_user_having_mix_encryption_passw_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/mixOrganization.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
        assertThat(organization.getUsers().get(0).isPasswordEncrypted()).isTrue();
        assertThat(organization.getUsers().get(1).isPasswordEncrypted()).isFalse();

    }

    @Test
    public void should_parse_organizationWithCycle_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/organizationWithCycle.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
    }

    @Test
    public void should_parse_OrganizationWithSpecialCharacters_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/OrganizationWithSpecialCharacters.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
    }

    @Test
    public void should_parse_ACME_work() throws Exception {
        //given
        String content = IOUtil.read(OrganizationParserTest.class.getResourceAsStream("/ACME.xml"));
        //when
        Organization organization = organizationParser.convert(content);
        //then
        assertThat(organization).isNotNull();
        checkWilliamJobs(organization.getUsers().get(0));
        checkAprilSanchez(organization.getUsers().get(1));
        assertThat(organization.getRoles()).hasSize(1).containsOnly(new ExportedRole("member", "Member", "this is the default member", "member icon", "member.png"));
        assertThat(organization.getGroups()).hasSize(13).contains(
                new ExportedGroup("headquarters", null, "Headquarters", "the main group", "acme logo", "acme.png"),
                new ExportedGroup("hr", "/headquarters", "Human Resources", null, null, null)
        );
        assertThat(organization.getMemberships()).contains(
                new ExportedUserMembership("william.jobs", "member", "headquarters", null, null, null),
                new ExportedUserMembership("april.sanchez", "member", "hr", "/headquarters", "william.jobs", 1457625147L)
        );
        assertThat(organization.getCustomUserInfoDefinition()).containsExactly(
                new ExportedCustomUserInfoDefinition("Office location", null),
                new ExportedCustomUserInfoDefinition("Skills", "The user skills")
        );
    }

    private void checkAprilSanchez(ExportedUser aprilSanchez) {
        assertThat(aprilSanchez.getUserName()).isEqualTo("april.sanchez");
        assertThat(aprilSanchez.isPasswordEncrypted()).isEqualTo(true);
        assertThat(aprilSanchez.getPassword()).isEqualTo("Zmdkc2E1NDM=");
        assertThat(aprilSanchez.isEnabled()).isEqualTo(false);
        assertThat(aprilSanchez.getManagerUserName()).isEqualTo("helen.kelly");
    }

    private void checkWilliamJobs(ExportedUser williamJobs) {
        assertThat(williamJobs.getUserName()).isEqualTo("william.jobs");
        assertThat(williamJobs.isPasswordEncrypted()).isEqualTo(false);
        assertThat(williamJobs.isEnabled()).isEqualTo(true);
        assertThat(williamJobs.getPassword()).isEqualTo("bpm");
        assertThat(williamJobs.getFirstName()).isEqualTo("William");
        assertThat(williamJobs.getLastName()).isEqualTo("Jobs");
        assertThat(williamJobs.getTitle()).isEqualTo("Mr");
        assertThat(williamJobs.getJobTitle()).isEqualTo("Chief Executive Officer");
        assertThat(williamJobs.getPersonalEmail()).isEqualTo("william.jobs@home.com");
        assertThat(williamJobs.getPersonalMobileNumber()).isEqualTo("333-222-3333");
        assertThat(williamJobs.getPersonalPhoneNumber()).isEqualTo("656-444-3333");
        assertThat(williamJobs.getPersonalBuilding()).isEqualTo("1");
        assertThat(williamJobs.getPersonalAddress()).isEqualTo("Infinite loop");
        assertThat(williamJobs.getPersonalZipCode()).isEqualTo("23000");
        assertThat(williamJobs.getPersonalCity()).isEqualTo("NY city");
        assertThat(williamJobs.getPersonalState()).isEqualTo("NY");
        assertThat(williamJobs.getPersonalCountry()).isEqualTo("United States");
        assertThat(williamJobs.getProfessionalEmail()).isEqualTo("william.jobs@acme.com");
        assertThat(williamJobs.getProfessionalPhoneNumber()).isEqualTo("484-302-5985");
        assertThat(williamJobs.getProfessionalFaxNumber()).isEqualTo("484-302-0985");
        assertThat(williamJobs.getProfessionalBuilding()).isEqualTo("70");
        assertThat(williamJobs.getProfessionalAddress()).isEqualTo("Renwick Drive");
        assertThat(williamJobs.getProfessionalZipCode()).isEqualTo("19108");
        assertThat(williamJobs.getProfessionalCity()).isEqualTo("Philadelphia");
        assertThat(williamJobs.getProfessionalState()).isEqualTo("PA");
        assertThat(williamJobs.getProfessionalCountry()).isEqualTo("United States");
        assertThat(williamJobs.getProfessionalRoom()).isEqualTo("213");
        assertThat(williamJobs.getCustomUserInfoValues()).containsExactly(
                new ExportedCustomUserInfoValue("Office location", "Engineering"),
                new ExportedCustomUserInfoValue("Skills", "Java")
        );
    }

}