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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "authorizationRules")
public class SPageMapping implements PersistentObject {

    public static final String COMMA_DELIM = ",";
    private long id;
    private long tenantId;
    private String key;
    private Long pageId;
    private String url;
    private String urlAdapter;
    private String pageAuthorizRules;
    private List<String> authorizationRules = new ArrayList<>();
    private long lastUpdateDate;
    private long lastUpdatedBy;

    private void parseRules() {
        if (pageAuthorizRules != null) {
            authorizationRules.clear();
            for (StringTokenizer stringTk = new StringTokenizer(pageAuthorizRules, COMMA_DELIM, false); stringTk
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
                pageAuthorizRules += (authorizationRule + COMMA_DELIM);
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
