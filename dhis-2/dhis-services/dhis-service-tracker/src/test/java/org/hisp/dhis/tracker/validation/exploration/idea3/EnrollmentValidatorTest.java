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
package org.hisp.dhis.tracker.validation.exploration.idea3;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1069;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1119;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1122;
import static org.hisp.dhis.tracker.validation.exploration.idea3.EnrollmentValidator.enrollmentValidator;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.hisp.dhis.utils.Assertions.assertIsEmpty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrollmentValidatorTest
{
    private static final MetadataIdentifier PROGRAM_ID = MetadataIdentifier.ofUid( "MNWZ6hnuhSw" );

    private TrackerPreheat preheat;

    private TrackerBundle bundle;

    private TrackerIdSchemeParams idSchemes;

    private ValidatorTree<Enrollment> validator;

    @BeforeEach
    void setUp()
    {

        idSchemes = TrackerIdSchemeParams.builder()
            .build();

        preheat = mock( TrackerPreheat.class );
        when( preheat.getIdSchemes() ).thenReturn( idSchemes );

        Program program = new Program();
        when( preheat.get( Program.class, PROGRAM_ID ) ).thenReturn( program );

        bundle = TrackerBundle.builder()
            .preheat( preheat )
            .build();

        validator = enrollmentValidator();
    }

    @Test
    void testValidEnrollment()
    {

        Enrollment enrollment = enrollment();

        List<Error> validation = validator.test( bundle, enrollment );

        assertIsEmpty( validation );
    }

    @Test
    void testInvalidEnrollmentWithMultipleErrors()
    {
        Enrollment enrollment = enrollment();
        // error E1048: invalid UID
        enrollment.setEnrollment( "invalid" );
        // error E1025: enrolledAt date is null
        enrollment.setEnrolledAt( null );
        // error E1119: duplicate note Kj6vYde4LHh
        when( preheat.getNote( "Kj6vYde4LHh" ) ).thenReturn( Optional.of( new TrackedEntityComment() ) );
        enrollment.setNotes( List.of(
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "olfXZzSGacW", "valid note" ),
            note( "invalid1", "note 1 with invalid uid" ),
            note( "invalid2", "note 2 with invalid uid" ) ) );

        List<Error> validation = validator.test( bundle, enrollment );

        assertFalse( validation.isEmpty() );
        List<TrackerErrorCode> errors = validation.stream().map( Error::getCode ).collect( Collectors.toList() );
        assertContainsOnly( List.of( E1048, E1025, E1119 ), errors );
    }

    @Test
    void testInvalidEnrollmentWithProgramNull()
    {
        Enrollment enrollment = enrollment();
        // validation error E1122: program not set
        enrollment.setProgram( MetadataIdentifier.EMPTY_UID );
        // validation error E1069: will not trigger as there is not even a
        // program to check in the preheat

        List<Error> validation = validator.test( bundle, enrollment );

        assertFalse( validation.isEmpty() );
        List<TrackerErrorCode> errors = validation.stream().map( Error::getCode ).collect( Collectors.toList() );
        assertContainsOnly( List.of( E1122 ), errors );
    }

    @Test
    void testInvalidEnrollmentWithProgramNotFound()
    {
        Enrollment enrollment = enrollment();
        // validation error E1069: program not found
        enrollment.setProgram( uid() );

        List<Error> validation = validator.test( bundle, enrollment );

        assertFalse( validation.isEmpty() );
        List<TrackerErrorCode> errors = validation.stream().map( Error::getCode ).collect( Collectors.toList() );
        assertContainsOnly( List.of( E1069 ), errors );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

    private static Enrollment enrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollment( CodeGenerator.generateUid() );
        enrollment.setProgram( PROGRAM_ID );
        enrollment.setOrgUnit( MetadataIdentifier.ofUid( "Nav6inZRw1u" ) );
        enrollment.setTrackedEntity( "PuBvJxDB73z" );
        enrollment.setEnrolledAt( Instant.now() );
        return enrollment;
    }

    private static MetadataIdentifier uid()
    {
        return MetadataIdentifier.ofUid( CodeGenerator.generateUid() );
    }

}