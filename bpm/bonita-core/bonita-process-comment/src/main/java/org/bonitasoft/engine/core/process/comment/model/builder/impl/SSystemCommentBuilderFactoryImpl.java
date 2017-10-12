/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.comment.model.builder.impl;

import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SSystemCommentBuilderFactory;
import org.bonitasoft.engine.core.process.comment.model.impl.SSystemCommentImpl;

/**
 * @author Hongwen Zang
 */
public class SSystemCommentBuilderFactoryImpl extends SCommentBuilderFactoryImpl implements SSystemCommentBuilderFactory {

    @Override
    public SCommentBuilder createNewInstance(final long processInstanceId, final String comment, final Long userId) {
        final SSystemCommentImpl sSystemCommentImpl = new SSystemCommentImpl(processInstanceId, comment);
        final long postDate = System.currentTimeMillis();
        sSystemCommentImpl.setPostDate(postDate);
        sSystemCommentImpl.setUserId(userId);
        return new SSystemCommentBuilderImpl(sSystemCommentImpl);
    }
}