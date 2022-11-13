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
package org.hisp.dhis.tracker.validation.experiment;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.hisp.dhis.tracker.validation.experiment.Validation.Fail.fail;
import static org.hisp.dhis.tracker.validation.experiment.Validation.Success.success;

import java.util.Objects;

import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.report.TrackerErrorCode;

// shows that in theory the validator can return a different type R as a result than what it validates
// we might not need this as we don't really care about the return value other than the Success
// as we do not partially apply a constructor that needs these.
// but an example is that a validator takes a String representing a date, parses it and if successful constructs a Date
public class DateValidator implements Validator<Enrollment, Enrollment, TrackerErrorCode>
{

    // TODO what if I want to just write validation functions as a bunch of
    // static methods
    // TODO in this case the validator would it be better written as
    // validate(Instant enrolledAt)? and then outside we would need to adapt it
    // so that the result of getEnrolledAt is passed in
    public Validation<Enrollment, TrackerErrorCode> validate( Enrollment enrollment )
    {
        if ( Objects.isNull( enrollment.getEnrolledAt() ) )
        {
            return fail( E1025 );
        }

        return success( enrollment );
    }
}
