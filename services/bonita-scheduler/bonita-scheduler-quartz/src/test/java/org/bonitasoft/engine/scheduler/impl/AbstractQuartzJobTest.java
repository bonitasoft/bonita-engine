/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.junit.Test;
import org.quartz.JobExecutionException;

/**
 * @author Baptiste Mesta
 */
public class AbstractQuartzJobTest {


    private AbstractQuartzJob abstractQuartzJob = new ConcurrentQuartzJob();

    @Test
    public void should_execute_set_exception_to_unschedule_in_case_of_exception() throws Exception {
        //given
        abstractQuartzJob.setBosJob(new StatelessJob() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void execute() throws SJobExecutionException, SFireEventException {
                throw new SJobExecutionException("exception");
            }

            @Override
            public void setAttributes(Map<String, Serializable> attributes) throws SJobConfigurationException {

            }
        });

        //when
        try{

            abstractQuartzJob.execute(null);
            fail("should throw exception");
        }catch (JobExecutionException e ){
            assertThat(e.unscheduleFiringTrigger()).isTrue();
        }


        //then

    }


}