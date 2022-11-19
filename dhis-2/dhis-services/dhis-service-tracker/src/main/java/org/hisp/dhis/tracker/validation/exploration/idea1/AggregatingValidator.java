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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.bundle.TrackerBundle;

// TODO implement short circuit behavior: either in here or make this a base class/interface with default implementations for
// and and then implement an AggregatingValidator and a FailFastValidator based on this
public class AggregatingValidator<T> implements Validator<T, List<Error>>
{

    // TODO: adding the validateEach complicated the rest of the code. Since I
    // want clients to be able to run
    // a Validator that is only able to deal with one input and return one error
    // I need to use closures that return a List<Validator> that are applied on
    // call to apply. So for each of the elements
    // in a collection there will be a Validator with a closure on the element.
    private final List<Function<T, List<Validator<T, Error>>>> validators = new ArrayList<>();

    public AggregatingValidator<T> validate( Validator<T, Error> validator )
    {
        // ignoring the input parameter using __ as _ is reserved and might
        // become the throwaway parameter
        validators.add( __ -> Collections.singletonList( validator ) );
        return this;
    }

    public <S> AggregatingValidator<T> validate( Function<T, S> map, Validator<S, Error> validator )
    {
        // ignoring the input parameter using __ as _ is reserved and might
        // become the throwaway parameter
        validators
            .add(
                __ -> Collections.singletonList( ( bundle, input ) -> validator.apply( bundle, map.apply( input ) ) ) );
        return this;
    }

    public <C, S> AggregatingValidator<T> validateEach( Function<T, Collection<C>> mapToEach, Function<C, S> map,
        Predicate<S> validator, Function<S, Error> error )
    {

        validators.add(
            input -> mapToEach.apply( input ).stream().map( i -> (Validator<T, Error>) ( bundle, __ ) -> {

                S mappedInput = map.apply( i );
                if ( validator.test( mappedInput ) )
                {
                    return Optional.empty();
                }

                return Optional.of( error.apply( mappedInput ) );

            } ).collect( Collectors.toList() ) );

        return this;
    }

    /**
     * Validate predicate evaluates to true for input mapped by given map
     * function from type T to type S. map is typically a method reference to a
     * getter on type T.
     *
     * @param map map type T to type S for which to ensure predicate holds true
     * @param validator predicate which should hold true otherwise an error is
     *        returned
     * @param error
     * @return
     * @param <S> type on which the predicate will be evaluated
     */
    public <S> AggregatingValidator<T> validate( Function<T, S> map, Predicate<S> validator, Function<S, Error> error )
    {
        // ignoring the input parameter using __ as _ is reserved and might
        // become the throwaway parameter
        validators.add( __ -> Collections.singletonList( ( bundle, input ) -> {

            S mappedInput = map.apply( input );
            if ( validator.test( mappedInput ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( mappedInput ) );

        } ) );
        return this;
    }

    public AggregatingValidator<T> validate( Predicate<T> validator, Function<T, Error> error )
    {
        // ignoring the input parameter using __ as _ is reserved and might
        // become the throwaway parameter
        validators.add( __ -> Collections.singletonList( ( bundle, input ) -> {

            if ( validator.test( input ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( input ) );

        } ) );
        return this;
    }

    @Override
    public Optional<List<Error>> apply( TrackerBundle bundle, T input )
    {
        List<Error> errors = new ArrayList<>();
        // TODO this can likely be written in a cleaner way
        for ( Function<T, List<Validator<T, Error>>> v : validators )
        {
            for ( Validator<T, Error> validator : v.apply( input ) )
            {
                Optional<Error> error = validator.apply( bundle, input );
                error.ifPresent( errors::add );
            }
        }

        if ( errors.isEmpty() )
        {
            return Optional.empty();
        }

        return Optional.of( errors );
    }
}
