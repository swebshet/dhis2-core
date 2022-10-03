/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.webapi.controller.event;

/*
 * Copyright (c) 2004-2022, University of Oslo
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.events.enrollment.AbstractEnrollmentService;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.Enrollments;
import org.hisp.dhis.dxf2.events.params.EnrollmentParams;
import org.hisp.dhis.fieldfilter.FieldFilterService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceQueryParams;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentity.TrackedEntityTypeService;
import org.hisp.dhis.user.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith( MockitoExtension.class )
class EnrollmentControllerTest
{

    @Mock
    AbstractEnrollmentService enrollmentService;

    @Mock
    ProgramInstanceService programInstanceService;

    @Mock
    FieldFilterService fieldFilterService;

    @Mock
    ProgramInstance programInstance;

    @Captor
    ArgumentCaptor<EnrollmentParams> entityInstanceParamsArgumentCaptor;

    String enrollmentId = CodeGenerator.generateUid();

    MockMvc mockMvc;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders
            .standaloneSetup( new EnrollmentController( null, enrollmentService, null, programInstanceService,
                fieldFilterService, null,
                new EnrollmentCriteriaMapper( mock( CurrentUserService.class ), mock( OrganisationUnitService.class ),
                    mock( ProgramService.class ),
                    mock( TrackedEntityTypeService.class ), mock( TrackedEntityInstanceService.class ) ) ) )
            .build();
    }

    @Test
    void givenNoInputFields_shouldUseDefaultParameters()
        throws Exception
    {
        when( programInstanceService.getProgramInstance( enrollmentId ) ).thenReturn( programInstance );

        when( enrollmentService.getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollment() );

        mockMvc.perform( get( "/enrollments/{id}", enrollmentId ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenRelationshipField_shouldSearchWithIncludeRelationships()
        throws Exception
    {
        when( programInstanceService.getProgramInstance( enrollmentId ) ).thenReturn( programInstance );

        when( enrollmentService.getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollment() );

        mockMvc.perform( get( "/enrollments/{id}", enrollmentId ).param( "fields", "relationships" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertTrue( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenEventsField_shouldSearchWithIncludeEvents()
        throws Exception
    {
        when( programInstanceService.getProgramInstance( enrollmentId ) ).thenReturn( programInstance );

        when( enrollmentService.getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollment() );

        mockMvc.perform( get( "/enrollments/{id}", enrollmentId ).param( "fields", "events" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenAttributesField_shouldSearchWithIncludeAttributes()
        throws Exception
    {
        when( programInstanceService.getProgramInstance( enrollmentId ) ).thenReturn( programInstance );

        when( enrollmentService.getEnrollment( eq( programInstance ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollment() );

        mockMvc.perform( get( "/enrollments/{id}", enrollmentId ).param( "fields", "attributes" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenRelationshipField_shouldSearchEnrollmentsWithIncludeRelationships()
        throws Exception
    {
        when( enrollmentService.getEnrollments( any( ProgramInstanceQueryParams.class ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollments() );

        mockMvc.perform( get( "/enrollments/" ).param( "fields", "relationships" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertTrue( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenRelationshipField_shouldSearchEnrollmentsWithIncludeEvents()
        throws Exception
    {
        when( enrollmentService.getEnrollments( any( ProgramInstanceQueryParams.class ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollments() );

        mockMvc.perform( get( "/enrollments/" ).param( "fields", "events" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeEvents() ) );
    }

    @Test
    void givenRelationshipField_shouldSearchEnrollmentsWithIncludeAttributes()
        throws Exception
    {
        when( enrollmentService.getEnrollments( any( ProgramInstanceQueryParams.class ),
            entityInstanceParamsArgumentCaptor.capture() ) ).thenReturn( new Enrollments() );

        mockMvc.perform( get( "/enrollments/" ).param( "fields", "events" ) )
            .andExpect( status().isOk() );

        EnrollmentParams trackedEntityInstanceParams = entityInstanceParamsArgumentCaptor.getValue();

        assertAll(
            () -> assertFalse( trackedEntityInstanceParams.isIncludeRelationships() ),
            () -> assertFalse( trackedEntityInstanceParams.isIncludeAttributes() ),
            () -> assertTrue( trackedEntityInstanceParams.isIncludeEvents() ) );
    }
}
