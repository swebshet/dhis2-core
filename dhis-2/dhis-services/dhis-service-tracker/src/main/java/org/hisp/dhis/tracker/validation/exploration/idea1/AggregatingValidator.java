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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.report.TrackerErrorCode;

// TODO implement short circuit behavior: either in here or make this a base class/interface with default implementations for
// and and then implement an AggregatingValidator and a FailFastValidator based on this
public class AggregatingValidator<T> implements Validator<T, List<TrackerErrorCode>>
{

    private final List<Validator<T, TrackerErrorCode>> validators = new ArrayList<>();

    public AggregatingValidator<T> and( Validator<T, TrackerErrorCode> other )
    {
        validators.add( other );
        return this;
    }

    // Same as above
    public <S> AggregatingValidator<T> and( Function<T, S> value, Validator<S, TrackerErrorCode> other )
    {
        validators.add( ( bundle, input ) -> other.apply( bundle, value.apply( input ) ) );
        return this;
    }

    public <S> AggregatingValidator<T> and( Function<T, S> value, Predicate<S> other, TrackerErrorCode error )
    {
        validators.add( ( bundle, input ) -> {

            if ( !other.test( value.apply( input ) ) )
            {
                return Optional.empty();
            }

            return Optional.of( error );

        } );
        return this;
    }

    @Override
    public Optional<List<TrackerErrorCode>> apply( TrackerBundle bundle, T input )
    {
        List<TrackerErrorCode> errors = new ArrayList<>();
        for ( Validator<T, TrackerErrorCode> validator : validators )
        {
            Optional<TrackerErrorCode> error = validator.apply( bundle, input );
            error.ifPresent( e -> errors.add( e ) );
        }

        if ( errors.isEmpty() )
        {
            return Optional.empty();
        }

        return Optional.of( errors );
    }
}
