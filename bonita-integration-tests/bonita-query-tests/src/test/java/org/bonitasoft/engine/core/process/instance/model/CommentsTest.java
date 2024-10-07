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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.groups.Tuple.tuple;
import static org.bonitasoft.engine.commons.Pair.pair;

import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.core.process.comment.model.SHumanComment;
import org.bonitasoft.engine.core.process.comment.model.SSystemComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.jdbc.JdbcRowMapper;
import org.bonitasoft.engine.test.persistence.repository.CommentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class CommentsTest {

    private static final long JACK_ID = 783L;
    private static final long JOHN_ID = 784L;
    private static final long PROCESS1_ID = 123L;
    private static final long PROCESS2_ID = 124L;
    @Inject
    private CommentRepository repository;
    @Inject
    private JdbcTemplate jdbcTemplate;

    //Those tests currently verify that the queries returning UserMemberships correctly retrieve the groupParentPath when building the Usermembership objects

    @Test
    public void should_getComments_of_process_instance() {
        repository.add(new SHumanComment(PROCESS1_ID, "comment1", JACK_ID));
        repository.add(new SHumanComment(PROCESS1_ID, "comment2", JOHN_ID));
        repository.add(new SHumanComment(PROCESS1_ID, "comment3", JACK_ID));
        repository.add(new SSystemComment(PROCESS1_ID, "comment4"));
        repository.add(new SHumanComment(PROCESS2_ID, "comment5", JACK_ID));

        assertThat(repository.getCommentsOfProcessInstance(PROCESS1_ID)).extracting("content", "userId", "class")
                .containsExactlyInAnyOrder(
                        tuple("comment1", JACK_ID, SHumanComment.class),
                        tuple("comment2", JOHN_ID, SHumanComment.class),
                        tuple("comment3", JACK_ID, SHumanComment.class),
                        tuple("comment4", null, SSystemComment.class));

    }

    @Test
    public void should_save_and_get_SComment() {
        SHumanComment comment1 = repository.add(new SHumanComment(PROCESS1_ID, "comment1", JACK_ID));
        SSystemComment comment2 = repository.add(new SSystemComment(PROCESS2_ID, "comment2"));
        repository.flush();

        Map<String, Object> comment1AsMap = jdbcTemplate
                .queryForObject(
                        "SELECT ID, KIND, CONTENT, POSTDATE, PROCESSINSTANCEID, USERID FROM process_comment WHERE processInstanceId = "
                                + PROCESS1_ID,
                        new JdbcRowMapper("ID", "POSTDATE", "PROCESSINSTANCEID", "USERID"));
        Map<String, Object> comment2AsMap = jdbcTemplate
                .queryForObject(
                        "SELECT ID, KIND, CONTENT, POSTDATE, PROCESSINSTANCEID, USERID FROM process_comment WHERE processInstanceId = "
                                + PROCESS2_ID,
                        new JdbcRowMapper("ID", "POSTDATE", "PROCESSINSTANCEID", "USERID"));

        assertThat(comment1AsMap).containsOnly(
                entry("ID", comment1.getId()),
                entry("KIND", "human"),
                entry("CONTENT", "comment1"),
                entry("POSTDATE", comment1.getPostDate()),
                entry("PROCESSINSTANCEID", PROCESS1_ID),
                entry("USERID", JACK_ID));
        assertThat(comment2AsMap).containsOnly(
                entry("ID", comment2.getId()),
                entry("KIND", "system"),
                entry("CONTENT", "comment2"),
                entry("POSTDATE", comment2.getPostDate()),
                entry("PROCESSINSTANCEID", PROCESS2_ID),
                entry("USERID", null));
    }

    @Test
    public void should_save_and_get_SAComment() {
        SAComment comment1 = repository.add(new SAComment(new SHumanComment(PROCESS1_ID, "comment1", JACK_ID)));
        SAComment comment2 = repository.add(new SAComment(new SSystemComment(PROCESS2_ID, "comment2")));
        repository.flush();

        PersistentObject comment1FromQuery = repository.selectOne("getArchivedCommentById",
                pair("id", comment1.getId()));
        PersistentObject comment2FromQuery = repository.selectOne("getArchivedCommentById",
                pair("id", comment2.getId()));
        Map<String, Object> comment1AsMap = jdbcTemplate
                .queryForObject(
                        "SELECT ID, SOURCEOBJECTID, ARCHIVEDATE, CONTENT, POSTDATE, PROCESSINSTANCEID, USERID FROM arch_process_comment WHERE processInstanceId = "
                                + PROCESS1_ID,
                        new JdbcRowMapper("ID", "SOURCEOBJECTID", "ARCHIVEDATE", "POSTDATE", "PROCESSINSTANCEID",
                                "USERID"));
        Map<String, Object> comment2AsMap = jdbcTemplate
                .queryForObject(
                        "SELECT ID, SOURCEOBJECTID, ARCHIVEDATE, CONTENT, POSTDATE, PROCESSINSTANCEID, USERID FROM arch_process_comment WHERE processInstanceId = "
                                + PROCESS2_ID,
                        new JdbcRowMapper("ID", "SOURCEOBJECTID", "ARCHIVEDATE", "POSTDATE", "PROCESSINSTANCEID",
                                "USERID"));

        assertThat(comment1FromQuery).isEqualTo(comment1);
        assertThat(comment2FromQuery).isEqualTo(comment2);
        assertThat(comment1AsMap).containsOnly(
                entry("ID", comment1.getId()),
                entry("SOURCEOBJECTID", 0L),
                entry("ARCHIVEDATE", 0L),
                entry("CONTENT", "comment1"),
                entry("POSTDATE", comment1.getPostDate()),
                entry("PROCESSINSTANCEID", PROCESS1_ID),
                entry("USERID", JACK_ID));
        assertThat(comment2AsMap).containsOnly(
                entry("ID", comment2.getId()),
                entry("SOURCEOBJECTID", 0L),
                entry("ARCHIVEDATE", 0L),
                entry("CONTENT", "comment2"),
                entry("POSTDATE", comment2.getPostDate()),
                entry("PROCESSINSTANCEID", PROCESS2_ID),
                entry("USERID", null));

    }

}
