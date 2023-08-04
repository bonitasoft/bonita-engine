/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.page;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "authorizationRules")
@Table(name = "page_mapping")
@IdClass(PersistentObjectId.class)
public class SPageMapping implements PersistentObject {

    public static final String COMMA_DELIMITER = ",";

    @Id
    private long tenantId;
    @Id
    private long id;

    @Column(name = "key_")
    private String key;
    @Column(name = "pageid")
    private Long pageId;
    private String url;
    @Column(name = "urladapter")
    private String urlAdapter;
    @Column(name = "lastupdatedate")
    private long lastUpdateDate;
    @Column(name = "lastupdatedby")
    private long lastUpdatedBy;

    @Column(name = "page_authoriz_rules")
    private String pageAuthorizRules;

    @Transient
    private List<String> authorizationRules = new ArrayList<>();

    private void parseRules() {
        if (pageAuthorizRules != null) {
            authorizationRules.clear();
            for (StringTokenizer stringTk = new StringTokenizer(pageAuthorizRules, COMMA_DELIMITER, false); stringTk
                    .hasMoreTokens();) {
                String rule = stringTk.nextToken();
                authorizationRules.add(rule);
            }
        }
    }

    private void buildRulesAsString() {
        pageAuthorizRules = null;
        if (authorizationRules != null && !authorizationRules.isEmpty()) {
            pageAuthorizRules = "";
            for (String authorizationRule : authorizationRules) {
                pageAuthorizRules += (authorizationRule + COMMA_DELIMITER);
            }
        }
    }

    public List<String> getPageAuthorizationRules() {
        parseRules(); // Need to do it here because Hibernate does not call setter to set fields from DB to Object
        return authorizationRules;
    }

    public void setPageAuthorizationRules(List<String> rules) {
        authorizationRules = rules;
        buildRulesAsString();
    }
}
