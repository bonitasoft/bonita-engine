/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;

public class OracleInterceptorTest {

    private String sql = "select * from ( select sgatewayin0_.id as col_0_0_ from flownode_instance sgatewayin0_ "
            + "where sgatewayin0_.kind='gate' and sgatewayin0_.stateId<>3 "
            + "and (sgatewayin0_.stateId<>61 or sgatewayin0_.hitBys like 'FINISH:%' "
            + "or sgatewayin0_.stateCategory='ABORTING' or sgatewayin0_.stateCategory='CANCELLING') "
            + "and sgatewayin0_.lastUpdateDate<:1  order by sgatewayin0_.id ) where rownum <= :2";

    private String preparedStatement = "select * from ( select sgatewayin0_.id as col_0_0_ from flownode_instance sgatewayin0_ "
            + "where sgatewayin0_.kind='gate' and sgatewayin0_.stateId<>3 "
            + "and (sgatewayin0_.stateId<>61 or sgatewayin0_.hitBys like ? "
            + "or sgatewayin0_.stateCategory='ABORTING' or sgatewayin0_.stateCategory='CANCELLING') "
            + "and sgatewayin0_.lastUpdateDate<:1  order by sgatewayin0_.id ) where rownum <= ?";

    private String preparedStatementWithConcat = "select * from ( select distinct sprocessde2_.id as id1_47_, "
            + "sprocessde2_.version as version14_47_ from actor sactor0_ cross join actormember sactormemb1_ "
            + "cross join process_definition sprocessde2_ where sactor0_.scopeId=sprocessde2_.processId and "
            + "sactor0_.id=sactormemb1_.actorId and (sactormemb1_.groupId in (select sgroup3_.id from group_ "
            + "sgroup3_ cross join group_ sgroup4_ where ((sgroup3_.parentPath||'/'||sgroup3_.name||'/') "
            + "like '/' || sgroup4_.name || '/' || '%' or sgroup3_.id=?) and sgroup4_.id=?)) and "
            + "(sactormemb1_.actorId not in  (select sactormemb5_.actorId from actormember sactormemb5_ "
            + "where sactormemb5_.groupId<>sactormemb1_.groupId and (sactormemb5_.groupId not in  "
            + "(select sgroup6_.id from group_ sgroup6_ cross join group_ sgroup7_ where "
            + "(sgroup6_.parentPath||'/'||sgroup6_.name||'/' like sgroup7_.name||'/'||'%' or sgroup6_.id=?) "
            + "and sgroup7_.id=?)))) order by sprocessde2_.name ASC, sprocessde2_.id ASC ) where rownum <= ?";
    private OracleInterceptor oracleInterceptor = new OracleInterceptor();

    @Test
    public void should_uppercase_like_on_sql() {
        assertThat(oracleInterceptor.onPrepareStatement(sql)).isEqualTo(
                "select * from ( select sgatewayin0_.id as col_0_0_ from flownode_instance sgatewayin0_ "
                        + "where sgatewayin0_.kind='gate' and sgatewayin0_.stateId<>3 "
                        + "and (sgatewayin0_.stateId<>61 or UPPER(sgatewayin0_.hitBys) like UPPER('FINISH:%') "
                        + "or sgatewayin0_.stateCategory='ABORTING' or sgatewayin0_.stateCategory='CANCELLING') "
                        + "and sgatewayin0_.lastUpdateDate<:1  order by sgatewayin0_.id ) where rownum <= :2");
    }

    @Test
    public void should_uppercase_like_on_prepared_statement() {
        assertThat(oracleInterceptor.onPrepareStatement(preparedStatement)).isEqualTo(
                "select * from ( select sgatewayin0_.id as col_0_0_ from flownode_instance sgatewayin0_ "
                        + "where sgatewayin0_.kind='gate' and sgatewayin0_.stateId<>3 "
                        + "and (sgatewayin0_.stateId<>61 or UPPER(sgatewayin0_.hitBys) like UPPER(?) "
                        + "or sgatewayin0_.stateCategory='ABORTING' or sgatewayin0_.stateCategory='CANCELLING') "
                        + "and sgatewayin0_.lastUpdateDate<:1  order by sgatewayin0_.id ) where rownum <= ?");
    }

    @Test
    public void should_uppercase_like_on_prepared_statement_with_concat() {
        assertThat(oracleInterceptor.onPrepareStatement(preparedStatementWithConcat)).isEqualTo(
                "select * from ( select distinct sprocessde2_.id as id1_47_, "
                        + "sprocessde2_.version as version14_47_ from actor sactor0_ cross join actormember sactormemb1_ "
                        + "cross join process_definition sprocessde2_ where sactor0_.scopeId=sprocessde2_.processId and "
                        + "sactor0_.id=sactormemb1_.actorId and (sactormemb1_.groupId in (select sgroup3_.id from group_ "
                        + "sgroup3_ cross join group_ sgroup4_ where ((UPPER(sgroup3_.parentPath||'/'||sgroup3_.name||'/')) "
                        + "like UPPER('/' || sgroup4_.name || '/' || '%') or sgroup3_.id=?) and sgroup4_.id=?)) and "
                        + "(sactormemb1_.actorId not in  (select sactormemb5_.actorId from actormember sactormemb5_ "
                        + "where sactormemb5_.groupId<>sactormemb1_.groupId and (sactormemb5_.groupId not in  "
                        + "(select sgroup6_.id from group_ sgroup6_ cross join group_ sgroup7_ where "
                        + "(UPPER(sgroup6_.parentPath||'/'||sgroup6_.name||'/') like UPPER(sgroup7_.name||'/'||'%') or sgroup6_.id=?) "
                        + "and sgroup7_.id=?)))) order by sprocessde2_.name ASC, sprocessde2_.id ASC ) where rownum <= ?");
    }
}
