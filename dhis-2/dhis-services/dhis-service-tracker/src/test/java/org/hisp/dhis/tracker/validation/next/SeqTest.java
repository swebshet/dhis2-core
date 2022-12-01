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
package org.hisp.dhis.tracker.validation.next;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1080;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1081;
import static org.hisp.dhis.tracker.validation.hooks.AssertValidationErrorReporter.hasTrackerError;
import static org.hisp.dhis.tracker.validation.next.Seq.seq;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.TrackerType;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.report.ValidationErrorReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeqTest
{

    private Enrollment enrollment;

    private ValidationErrorReporter reporter;

    private TrackerBundle bundle;

    @BeforeEach
    void setUp()
    {
        TrackerIdSchemeParams idSchemes = TrackerIdSchemeParams.builder().build();
        reporter = new ValidationErrorReporter( idSchemes );
        bundle = TrackerBundle.builder().build();
        enrollment = enrollment();
    }

    @Test
    void testSeqCallsUntilFirstError()
    {
        Validator<Enrollment> validator = seq( Enrollment.class,
            ( r, b, e ) -> r.addError( enrollment, E1080 ),
            ( r, b, e ) -> r.addError( enrollment, E1081 ) );

        validator.apply( reporter, bundle, enrollment );

        hasTrackerError( reporter, E1080, TrackerType.ENROLLMENT, enrollment.getUid() );
        assertFalse( reporter.hasErrorReport( e -> e.getErrorCode() == E1081 ) );
    }

    @Test
    void testSeqNested()
    {
        Validator<Enrollment> validator = seq( Enrollment.class,
            ( r, b, e ) -> {
            }, // no error so moving on to the next sequence
            seq( Enrollment.class,
                ( r, b, e ) -> {
                }, // no error so moving on to the next sequence
                seq( Enrollment.class,
                    ( r, b, e ) -> r.addError( enrollment, E1080 ),
                    ( r, b, e ) -> r.addError( enrollment, E1081 ) ) ) );

        validator.apply( reporter, bundle, enrollment );

        hasTrackerError( reporter, E1080, TrackerType.ENROLLMENT, enrollment.getUid() );
        assertFalse( reporter.hasErrorReport( e -> e.getErrorCode() == E1081 ) );
    }

    private static Enrollment enrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollment( CodeGenerator.generateUid() );
        return enrollment;
    }
}