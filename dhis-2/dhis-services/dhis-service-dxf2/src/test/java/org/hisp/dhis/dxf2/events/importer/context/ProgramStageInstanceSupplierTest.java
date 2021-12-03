/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.dxf2.events.importer.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.program.ProgramStageInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * @author Luciano Fiandesio
 */
public class ProgramStageInstanceSupplierTest
    extends AbstractSupplierTest<ProgramStageInstance, Map<String, ProgramStageInstance>>
{
    private ProgramStageInstanceSupplier subject;

    @Mock
    private ProgramEventSupplier programSupplier;

    @Before
    public void setUp()
    {
        JsonMapper mapper = new JsonMapper();
        this.subject = new ProgramStageInstanceSupplier( jdbcTemplate, mapper, programSupplier );
    }

    @Test
    public void handleNullEvents()
    {
        assertNotNull( subject.get( ImportOptions.getDefaultImportOptions(), null ) );
    }

    @Override
    public void verifySupplier()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "programstageinstanceid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "status" ) ).thenReturn( "ACTIVE" );
        when( mockResultSet.getBoolean( "deleted" ) ).thenReturn( false );

        // create event to import
        Event event = new Event();
        event.setUid( CodeGenerator.generateUid() );
        event.setEnrollment( "abcded" );

        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );

        Map<String, ProgramStageInstance> map = subject.get( ImportOptions.getDefaultImportOptions(),
            Collections.singletonList( event ) );

        ProgramStageInstance programStageInstance = map.get( "abcded" );
        assertThat( programStageInstance, is( notNullValue() ) );
        assertThat( programStageInstance.getId(), is( 100L ) );
        assertThat( programStageInstance.getUid(), is( "abcded" ) );
        assertThat( programStageInstance.getStatus(), is( EventStatus.ACTIVE ) );
        assertThat( programStageInstance.isDeleted(), is( false ) );
    }
}
