/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */

package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;

import javax.inject.Inject;

import org.assertj.core.groups.Tuple;
import org.bonitasoft.engine.core.process.comment.model.SHumanComment;
import org.bonitasoft.engine.core.process.comment.model.SSystemComment;
import org.bonitasoft.engine.test.persistence.repository.CommentRepository;
import org.bonitasoft.engine.test.persistence.repository.RoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"/testContext.xml"})
@Transactional
public class CommentsTest {

    public static final long JACK_ID = 783L;
    public static final long JOHN_ID = 784L;
    public static final long PROCESS1_ID = 123L;
    public static final long PROCESS2_ID = 124L;
    @Inject
    private CommentRepository repository;

    //Those tests currently verify that the queries returning UserMemberships correctly retrieve the groupParentPath when building the Usermembership objects

    @Test
    public void should_getComments_of_process_instance() {
        repository.add(new SHumanComment(PROCESS1_ID, "comment1", JACK_ID));
        repository.add(new SHumanComment(PROCESS1_ID, "comment2", JOHN_ID));
        repository.add(new SHumanComment(PROCESS1_ID, "comment3", JACK_ID));
        repository.add(new SSystemComment(PROCESS1_ID, "comment4"));
        repository.add(new SHumanComment(PROCESS2_ID, "comment5", JACK_ID));

        assertThat(repository.getCommentsOfProcessInstance(PROCESS1_ID)).extracting("content", "userId", "class").containsExactly(
                tuple("comment1", JACK_ID, SHumanComment.class),
                tuple("comment2", JOHN_ID, SHumanComment.class),
                tuple("comment3", JACK_ID, SHumanComment.class),
                tuple("comment4", null, SSystemComment.class)
        );

    }

}
