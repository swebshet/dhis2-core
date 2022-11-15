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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.report.TrackerErrorCode;

// TODO this one here could actually be defined as Validator<Enrollment, List<TrackerErrorCode>> so I can implement the aggregation
public class EnrollmentValidator implements Validator<Enrollment, TrackerErrorCode>
{

    private final List<Validator<Enrollment, TrackerErrorCode>> validators = new ArrayList<>();

    // TODO validators should be composable, so return type
    // Validator<Enrollment, TrackerErrorCode>
    // should work
    // there should be a default implementation as the Validator interface
    // should stay functional meaning I want people
    // to also be able to implement one via a lambda
    // How does that play with the idea to return Validator<Enrollment,
    // List<TrackerErrorCode>> from here?
    public EnrollmentValidator and( Validator<Enrollment, TrackerErrorCode> other )
    {
        validators.add( other );
        return this;
    }

    // Same as above
    public <S> EnrollmentValidator and( Function<Enrollment, S> value, Validator<S, TrackerErrorCode> other )
    {
        validators.add( e -> other.apply( value.apply( e ) ) );
        return this;
    }

    @Override
    public Optional<TrackerErrorCode> apply( Enrollment input )
    {

        Optional<TrackerErrorCode> error = Optional.empty();
        for ( Validator<Enrollment, TrackerErrorCode> validator : validators )
        {
            error = validator.apply( input );
            // TODO implement short circuit behavior
        }

        // TODO implement aggregation of errors
        return error;
    }
}
