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

// TODO I only used the name ValidatorTree as the ValidatorNode name is already taken by the interface
// otherwise I would call this a ValidatorNode
/**
 * ValidatorTree is a hierarchical {@link Validator}.
 *
 * @param <T> type of entity to be validated
 */
public class ValidatorTree<T> implements ValidatorNode<T>
{
    private ValidatorNode<T> parent;

    // TODO this represents a Validator that returns a single error. this will
    // not work for a collection validator
    // to align with the collection validator this validator needs to be wrapped
    // so it returns a Node<Optional<Error>>
    // the node will just be a single node. This is why I wanted to change the
    // signature to
    // private final Validator<T, Node<Optional<Error>>> validator;
    private final Validator<T, Optional<Error>> validator;

    private final List<ValidatorNode<T>> children = new ArrayList<>();

    public ValidatorTree()
    {
        this.parent = null;
        this.validator = ( bundle, input ) -> Optional.empty();
    }

    public ValidatorTree( Validator<T, Optional<Error>> validator )
    {
        this( null, validator );
    }

    public ValidatorTree( ValidatorNode<T> parent, Validator<T, Optional<Error>> validator )
    {
        this.parent = parent;
        this.validator = validator;
    }

    public ValidatorTree( SimpleValidator<T> validator )
    {
        this( null, validator );
    }

    public ValidatorTree( ValidatorNode<T> parent, SimpleValidator<T> validator )
    {
        this.parent = parent;
        this.validator = validator;
    }

    public ValidatorTree( Predicate<T> validator, BiFunction<TrackerIdSchemeParams, T, Error> error )
    {
        this.parent = null;
        this.validator = ( bundle, input ) -> {

            if ( validator.test( input ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), input ) );
        };
    }

    public ValidatorTree( ValidatorNode<T> parent, Predicate<T> validator,
        BiFunction<TrackerIdSchemeParams, T, Error> error )
    {
        this.parent = parent;
        this.validator = ( bundle, input ) -> {

            if ( validator.test( input ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), input ) );
        };
    }

    public <S> ValidatorTree( Function<T, S> map, Predicate<S> validator,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        this( null, map, validator, error );
    }

    public <S> ValidatorTree( ValidatorNode<T> parent, Function<T, S> map, Predicate<S> validator,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        this.parent = parent;
        this.validator = ( bundle, input ) -> {
            S mappedInput = map.apply( input );
            if ( validator.test( mappedInput ) )
            {
                return Optional.empty();
            }

            return Optional.of( error.apply( bundle.getPreheat().getIdSchemes(), mappedInput ) );
        };
    }

    @Override
    public void setParent( ValidatorNode<T> parent )
    {
        this.parent = parent;
    }

    // TODO without the hack of passing in the class Java cannot infer the type
    public static <T> ValidatorTree<T> validate( Class<T> klass )
    {
        return new ValidatorTree<>();
    }

    // TODO these nodes will have no parent; do I want root nodes that have a
    // validator in them?
    public static <T> ValidatorTree<T> validate( SimpleValidator<T> validator )
    {
        return new ValidatorTree<>( validator );
    }

    public static <T, S> ValidatorTree<T> validate( Function<T, S> map, Predicate<S> validator,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        return new ValidatorTree<>( map, validator, error );
    }

    public static <T> ValidatorTree<T> validate( Predicate<T> validator,
        BiFunction<TrackerIdSchemeParams, T, Error> error )
    {
        return new ValidatorTree<>( validator, error );
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public ValidatorTree<T> andThen( Validator<T, Optional<Error>> after )
    {
        this.children.add( new ValidatorTree<>( this, after ) );
        return this;
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public <S> ValidatorTree<T> andThen( Function<T, S> map, Validator<S, Optional<Error>> after )
    {
        this.children.add( new ValidatorTree<>( this,
            ( bundle, input ) -> after.apply( bundle, map.apply( input ) ) ) );
        return this;
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public <S> ValidatorTree<T> andThen( Function<T, S> map, ValidatorNode<S> after )
    {
        // TODO can this even work?
        this.children.add( new ValidatorTree<>( this,
            ( bundle, input ) -> after.apply( bundle, map.apply( input ) ).get() ) );
        return this;
    }

    /**
     * Validate after only if this validation does not return an error.
     *
     * @param after validator to apply after this validator
     * @return
     */
    public ValidatorTree<T> andThen( ValidatorNode<T> after )
    {
        after.setParent( this );
        this.children.add( after );
        return this;
    }

    public ValidatorTree<T> andThen( Predicate<T> after, BiFunction<TrackerIdSchemeParams, T, Error> error )
    {
        this.children.add( new ValidatorTree<>( this, after, error ) );
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
    public <S> ValidatorTree<T> andThen( Function<T, S> map, Predicate<S> after,
        BiFunction<TrackerIdSchemeParams, S, Error> error )
    {
        this.children.add( new ValidatorTree<>( this, map, after, error ) );
        return this;
    }

    @Override
    public ErrorNode apply( TrackerBundle bundle, T input )
    {
        return this.apply( this, bundle, input );
    }

    public ErrorNode apply( ValidatorNode<T> root, TrackerBundle bundle, T input )
    {
        ErrorNode result = null;
        Node<Validator<T, ErrorNode>> current;
        Stack<Node<Validator<T, ErrorNode>>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            // TODO should I just exclude calls to a root validator?
            //
            ErrorNode error = current.get().apply( bundle, input );

            // TODO this is to accommodate the root Validator which should end
            // up as a root
            // on the result ErrorNode and not be a child of an empty ErrorNode
            if ( result == null )
            {
                result = error;
            }
            else
            {
                result.add( error );
            }

            if ( error.hasError() )
            {
                // only visit children of valid parents
                continue;
            }

            for ( Node<Validator<T, ErrorNode>> child : current.getChildren() )
            {
                stack.push( child );
            }
        }
        return result;
    }

    public void apply( TrackerBundle bundle, T input, Consumer<ErrorNode> consumer )
    {
        traverseDepthFirst( this, validator -> {

            ErrorNode error = validator.apply( bundle, input );

            consumer.accept( error );

            // only visit children of valid parents
            return error.hasError();
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
    public void traverseDepthFirst( ValidatorNode<T> root, Function<Validator<T, ErrorNode>, Boolean> visit )
    {

        Node<Validator<T, ErrorNode>> current;
        Stack<Node<Validator<T, ErrorNode>>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            if ( Boolean.FALSE.equals( visit.apply( current.get() ) ) )
            {
                // only visit children of valid parents
                continue;
            }

            for ( Node<Validator<T, ErrorNode>> child : current.getChildren() )
            {
                stack.push( child );
            }
        }
    }

    // TODO this is just a helper for testing right now. Not sure yet how this
    // should look like
    // also since this is likely where fail fast will come into play as well
    public List<Error> test( TrackerBundle bundle, T input )
    {
        List<Error> errs = new ArrayList<>();
        // TODO need to flatten the ErrorNode
        // maybe I should add such a method to the Node interface itself as
        // default
        // this.apply( bundle, input, o -> o.ifPresent( errs::add ) );
        return errs;
    }

    @Override
    public Validator<T, ErrorNode> get()
    {
        return this;
    }

    @Override
    public List<? extends Node<Validator<T, ErrorNode>>> getChildren()
    {
        return children;
    }
}
