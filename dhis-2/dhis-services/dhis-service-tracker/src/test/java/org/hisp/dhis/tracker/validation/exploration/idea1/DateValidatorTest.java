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

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.junit.jupiter.api.Test;

class DateValidatorTest
{
    @Test
    void testValidationFailsIfEnrolledAtDateIsNull()
    {

        DateValidator validator = new DateValidator();

        Enrollment enrollment = new Enrollment();

        Optional<TrackerErrorCode> validation = validator.apply( enrollment );

        assertFalse( validation.isEmpty() );
        assertEquals( E1025, validation.get() );
    }

    @Test
    void testValidationSucceedsIfEnrolledAtDateIsNotNull()
    {

        DateValidator validator = new DateValidator();

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt( Instant.now() );

        Optional<TrackerErrorCode> validation = validator.apply( enrollment );

        assertTrue( validation.isEmpty() );
    }
}