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
package org.hisp.dhis.tracker.validation.exploration.idea1;

import static java.util.function.Predicate.not;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1119;
import static org.hisp.dhis.tracker.validation.exploration.idea1.DuplicateNotesValidator.noDuplicateNotes;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.trackedentitycomment.TrackedEntityComment;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.ValidationErrorReporter;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentDateValidationHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Shows how client code using the {@link AggregatingValidator} would look like.
 *
 * Validations here include validations we have for example in
 * {@link EnrollmentDateValidationHook#validateMandatoryDates(ValidationErrorReporter, Enrollment)}
 * {@link org.hisp.dhis.tracker.validation.hooks.EnrollmentNoteValidationHook}
 */
class AggregatingValidatorTest
{

    private TrackerPreheat preheat;

    private TrackerBundle bundle;

    @BeforeEach
    void setUp()
    {

        preheat = mock( TrackerPreheat.class );
        bundle = TrackerBundle.builder()
            .preheat( preheat )
            .build();
    }

    @Test
    void testValidationWithMultipleErrors()
    {
        Enrollment enrollment = new Enrollment();

        // validation error: enrolledAt date is null

        // validation error: duplicate note
        when( preheat.getNote( "Kj6vYde4LHh" ) ).thenReturn( Optional.of( new TrackedEntityComment() ) );
        enrollment.setNotes( List.of( Note.builder().note( "Kj6vYde4LHh" ).value( "my duplicate note" ).build() ) );

        // TODO how to make the code easier to read in terms the methods being
        // the negation of what i would expect.
        // validate(not(isValdUid))
        // should actually read
        // validate(isValidUid)
        // or is another name than validate better?
        AggregatingValidator<Enrollment> validator = new AggregatingValidator<Enrollment>()
            .validate( Enrollment::getEnrollment, not( CodeGenerator::isValidUid ), E1048 )
            .validate( Enrollment::getEnrolledAt, Objects::isNull, E1025 ) // EnrollmentDateValidationHook.validateMandatoryDates
            .validate( Enrollment::getNotes, noDuplicateNotes() );

        Optional<List<TrackerErrorCode>> validation = validator.apply( bundle, enrollment );

        assertFalse( validation.isEmpty() );
        List<TrackerErrorCode> errors = validation.get();
        assertContainsOnly( errors, List.of( E1025, E1119 ) );
    }

}