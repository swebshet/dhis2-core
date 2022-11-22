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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;

// TODO generalize this; to get to my idea of the tree.
// If some nodes func are only transformations than the children need to match the return type R of the func of their
// parent.
// if the node itself

// TODO if the Validator interface would be generic on its return type then this could also implement it
// not sure what interesting capabilities we would get from it :)
public class ValidatorNode<T> implements Node<Validator<T>>
{
    // TODO this is what I had initially. BiFunction<TrackerBundle, T,
    // Optional<Error>> would work
    // private final Function<T, Optional<Error>> func;
    private final Validator<T> validator;

    private final List<ValidatorNode<T>> children = new ArrayList<>();

    public ValidatorNode()
    {
        this.validator = ( bundle, input ) -> Optional.empty();
    }

    public ValidatorNode( Validator<T> validator )
    {
        this.validator = validator;
    }

    public ValidatorNode( SimpleValidator<T> validator )
    {
        this.validator = validator;
    }

    // TODO this does not work without casting hell. Try again if it works now
    public static <T> ValidatorNode<T> validate()
    {
        return new ValidatorNode<>( input -> Optional.empty() );
    }

    public static <T> ValidatorNode<T> validate( SimpleValidator<T> validator )
    {
        return new ValidatorNode<>( validator );
    }

    public static <T, S> ValidatorNode<T> validate( Function<T, S> map, Predicate<S> validator,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        return new ValidatorNode<>( ( bundle, input ) -> {

            S mappedInput = map.apply( input );
            if ( validator.test( mappedInput ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), mappedInput ) );
        } );
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public ValidatorNode<T> andThen( Validator<T> after )
    {
        this.children.add( new ValidatorNode<>( after ) );
        return this;
    }

    // TODO I could add an andThen(Function<T,S> map, ValidatorNode<S> after)
    // where map is applied to the node and its children

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public <S> ValidatorNode<T> andThen( Function<T, S> map, Validator<S> after )
    {
        this.children.add( new ValidatorNode<>(
            ( bundle, input ) -> after.apply( bundle, map.apply( input ) ) ) );
        return this;
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public ValidatorNode<T> andThen( ValidatorNode<T> after )
    {
        this.children.add( after );
        return this;
    }

    public ValidatorNode<T> andThen( Predicate<T> after, BiFunction<TrackerIdSchemeParams, T, Error> error )
    {

        this.children.add( new ValidatorNode<>( ( bundle, input ) -> {

            if ( after.test( input ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), input ) );

        } ) );
        return this;
    }

    /**
     * Validate predicate evaluates to true for input mapped by given map
     * function from type T to type S. map is typically a method reference to a
     * getter on type T.
     *
     * @param map map type T to type S for which to ensure predicate holds true
     * @param after predicate which should hold true otherwise an error is
     *        returned
     * @param error
     * @return
     * @param <S> type on which the predicate will be evaluated
     */
    public <S> ValidatorNode<T> andThen( Function<T, S> map, Predicate<S> after,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        this.children.add( new ValidatorNode<>( ( bundle, input ) -> {

            S mappedInput = map.apply( input );
            if ( after.test( mappedInput ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), mappedInput ) );

        } ) );
        return this;
    }

    public void visit( TrackerBundle bundle, T input, Consumer<Optional<Error>> consumer )
    {
        visit( this, bundle, input, consumer );
    }

    // TODO is this map or rather visit? I think for map it needs to take a
    // Function<T, S> so be more generic
    public void visit( ValidatorNode<T> root, TrackerBundle bundle, T input, Consumer<Optional<Error>> consumer )
    {
        if ( root == null )
        {
            return;
        }

        Optional<Error> optionalError = root.validator.apply( bundle, input );
        consumer.accept( optionalError );

        // TODO there might be value in visiting all nodes or only the behavior
        // of valid roots children
        // lets make it work for the default

        // only visit children of valid parents
        if ( optionalError.isPresent() )
        {
            return;
        }

        for ( ValidatorNode<T> child : root.children )
        {
            visit( child, bundle, input, consumer );
        }
    }

    @Override
    public Validator<T> get()
    {
        return validator;
    }

    // TODO pass in bundle
    public Node<Optional<Error>> apply( TrackerBundle bundle, T input )
    {
        ErrorNode result = new ErrorNode();

        this.visit( this, bundle, input, opt -> result.add( new ErrorNode( opt ) ) );

        return result;
    }

    // TODO this is just a helper for testing right now. Not sure yet how this
    // should look like
    // also since this is likely where fail fast will come into play as well
    public List<Error> validate( TrackerBundle bundle, T input )
    {
        List<Error> errs = new ArrayList<>();
        this.visit( bundle, input, o -> o.ifPresent( errs::add ) );
        return errs;
    }

}
