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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;

// TODO I think a ValidatorNode could/maybe should actually be a Validator
// issue is with the interface signatures as the return type is not part of the signature
// there is a collision unless I rename one of them
// Validator: Optional<Error> apply( TrackerBundle bundle, T input );
// (Validator)Node: Node<Validator<<Error>> apply( TrackerBundle bundle, T input );
//
// like
// implements Node<Validator<T>>, Validator<T, List<Optional<Error>>>
// this would mean that the Validator interface gets more generic; not
// sure if there is a way to circumvent most Validator implementations having to deal with
// this
/**
 * ValidatorNode is a hierarchical {@link Validator}.
 *
 * @param <T> type of entity to be validated
 */
public class ValidatorNode<T> implements Node<Validator<T>>
{
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

    // TODO without the hack of passing in the class Java cannot infer the type
    public static <T> ValidatorNode<T> validate( Class<T> klass )
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

    public static <T> ValidatorNode<T> validate( Predicate<T> validator,
        BiFunction<TrackerIdSchemeParams, T, Error> error )
    {
        return new ValidatorNode<>( ( bundle, input ) -> {

            if ( validator.test( input ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), input ) );
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
        this.children.add( validate( after, error ) );
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
     * @param <S> type on which the predicate will be evaluated
     * @return
     */
    public <S> ValidatorNode<T> andThen( Function<T, S> map, Predicate<S> after,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        this.children.add( validate( map, after, error ) );
        return this;
    }

    @Override
    public Validator<T> get()
    {
        return validator;
    }

    public ErrorNode apply( TrackerBundle bundle, T input )
    {
        return this.apply( this, bundle, input );
    }

    public ErrorNode apply( ValidatorNode<T> root, TrackerBundle bundle, T input )
    {
        ErrorNode result = null;
        ValidatorNode<T> current;
        Stack<ValidatorNode<T>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            Optional<Error> error = current.validator.apply( bundle, input );
            // TODO this is to accommodate the root Validator which should end
            // up as a root
            // on the result ErrorNode and not be a child of an empty ErrorNode
            if ( result == null )
            {
                result = new ErrorNode( error );
            }
            else
            {
                result.add( new ErrorNode( error ) );
            }

            if ( error.isPresent() )
            {
                // only visit children of valid parents
                continue;
            }

            for ( ValidatorNode<T> child : current.children )
            {
                stack.push( child );
            }
        }
        return result;
    }

    public void apply( TrackerBundle bundle, T input, Consumer<Optional<Error>> consumer )
    {
        traverseDepthFirst( this, validator -> {
            Optional<Error> error = validator.apply( bundle, input );
            consumer.accept( error );

            // only visit children of valid parents
            return error.isEmpty();
        } );
    }

    /**
     * Traverse the node in depth-first order. Validators are passed to the
     * visit function. Children will not be visited if visit returns false.
     *
     * @param root traverse node and its children
     * @param visit called with the current validator, skip visiting children on
     *        false
     */
    public void traverseDepthFirst( ValidatorNode<T> root, Function<Validator<T>, Boolean> visit )
    {

        ValidatorNode<T> current;
        Stack<ValidatorNode<T>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            if ( Boolean.FALSE.equals( visit.apply( current.validator ) ) )
            {
                // only visit children of valid parents
                continue;
            }

            for ( ValidatorNode<T> child : current.children )
            {
                stack.push( child );
            }
        }
    }

    // TODO this is just a helper for testing right now. Not sure yet how this
    // should look like
    // also since this is likely where fail fast will come into play as well
    public List<Error> validate( TrackerBundle bundle, T input )
    {
        List<Error> errs = new ArrayList<>();
        this.apply( bundle, input, o -> o.ifPresent( errs::add ) );
        return errs;
    }

}
