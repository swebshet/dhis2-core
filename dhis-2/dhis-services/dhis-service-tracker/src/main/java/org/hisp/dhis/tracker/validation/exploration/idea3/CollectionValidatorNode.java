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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hisp.dhis.tracker.bundle.TrackerBundle;

/**
 * CollectionValidatorNode is a hierarchical {@link Validator} that applies each
 * a Validator to each element of type T in a collection of type T.
 *
 * @param <T> type of entity to be validated
 */
public class CollectionValidatorNode<T> implements Node<Validator<Collection<? extends T>>>
{

    private final Validator<T> validator;

    private final List<CollectionValidatorNode<T>> children = new ArrayList<>();

    public CollectionValidatorNode( Validator<T> validator )
    {
        this.validator = validator;
    }

    @Override
    public Validator<Collection<? extends T>> get()
    {
        // TODO not sure how to satisfy this
        // TODO clean up the interface business. So far I think its not needed
        return null;
    }

    public ErrorNode apply( TrackerBundle bundle, Collection<? extends T> input )
    {
        return this.apply( this, bundle, input );
    }

    public ErrorNode apply( CollectionValidatorNode<T> root, TrackerBundle bundle, Collection<? extends T> input )
    {
        ErrorNode result = null;
        CollectionValidatorNode<T> current;
        Stack<CollectionValidatorNode<T>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            // map over input collection and apply validator to each element
            // create a node collecting all the nodes into children of ErrorNode

            // TODO this is to accommodate the root Validator which should end
            // up as a root
            // on the result ErrorNode and not be a child of an empty ErrorNode
            if ( result == null )
            {
                result = new ErrorNode();
            }
            boolean skipChildren = false;
            for ( T in : input )
            {
                Optional<Error> error = root.validator.apply( bundle, in );
                if ( error.isPresent() )
                {
                    skipChildren = true;
                }
                result.add( new ErrorNode( error ) );
            }

            // only visit children of valid parents
            if ( skipChildren )
            {
                continue;
            }

            for ( CollectionValidatorNode<T> child : current.children )
            {
                stack.push( child );
            }
        }
        return result;
    }

    public void apply( TrackerBundle bundle, Collection<? extends T> input, Consumer<Optional<Error>> consumer )
    {
        traverseDepthFirst( this, validator -> {
            boolean skipChildren = false;
            for ( T in : input )
            {
                Optional<Error> error = validator.apply( bundle, in );
                if ( error.isPresent() )
                {
                    skipChildren = true;
                }
                consumer.accept( error );
            }

            // only visit children of valid parents
            return !skipChildren;
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
    public void traverseDepthFirst( CollectionValidatorNode<T> root, Function<Validator<T>, Boolean> visit )
    {

        CollectionValidatorNode<T> current;
        Stack<CollectionValidatorNode<T>> stack = new Stack<>();
        stack.push( root );

        while ( !stack.empty() )
        {
            current = stack.pop();

            if ( Boolean.FALSE.equals( visit.apply( current.validator ) ) )
            {
                // skip visiting children
                continue;
            }

            for ( CollectionValidatorNode<T> child : current.children )
            {
                stack.push( child );
            }
        }
    }

    // TODO this is just a helper for testing right now. Not sure yet how this
    // should look like
    // also since this is likely where fail fast will come into play as well
    public List<Error> validate( TrackerBundle bundle, Collection<? extends T> input )
    {
        List<Error> errs = new ArrayList<>();
        this.apply( bundle, input, o -> o.ifPresent( errs::add ) );
        return errs;
    }
}
