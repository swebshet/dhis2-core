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
package org.hisp.dhis.tracker.validation.exploration.idea2;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1119;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1122;
import static org.hisp.dhis.tracker.validation.exploration.idea1.EnrollmentValidator.enrollmentValidator;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.validation.exploration.idea1.AggregatingValidator;
import org.hisp.dhis.tracker.validation.exploration.idea1.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrollmentValidatorTest
{
    private TrackerPreheat preheat;

    private TrackerBundle bundle;

    private TrackerIdSchemeParams idSchemes;

    @BeforeEach
    void setUp()
    {

        idSchemes = TrackerIdSchemeParams.builder()
            .build();

        preheat = mock( TrackerPreheat.class );
        when( preheat.getIdSchemes() ).thenReturn( idSchemes );

        bundle = TrackerBundle.builder()
            .preheat( preheat )
            .build();
    }

    @Test
    void testEnrollmentWithMultipleErrors()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setOrgUnit( MetadataIdentifier.ofUid( "Nav6inZRw1u" ) );
        // validation error: program not set
        enrollment.setProgram( MetadataIdentifier.EMPTY_UID );
        enrollment.setTrackedEntity( "PuBvJxDB73z" );

        // validation error: invalid UID
        enrollment.setEnrollment( "invalid" );

        // validation error: enrolledAt date is null

        // validation error: duplicate note Kj6vYde4LHh
        when( preheat.getNote( "Kj6vYde4LHh" ) ).thenReturn( Optional.of( new TrackedEntityComment() ) );
        enrollment.setNotes( List.of(
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "olfXZzSGacW", "valid note" ),
            note( "invalid1", "note 1 with invalid uid" ),
            note( "invalid2", "note 2 with invalid uid" ) ) );

        AggregatingValidator<Enrollment> validator = enrollmentValidator();

        Optional<List<Error>> validation = validator.apply( bundle, enrollment );

        assertFalse( validation.isEmpty() );
        List<TrackerErrorCode> errors = validation.get().stream().map( Error::getCode ).collect( Collectors.toList() );
        assertContainsOnly( List.of( E1048, E1048, E1048, E1122, E1025, E1119 ), errors );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

}