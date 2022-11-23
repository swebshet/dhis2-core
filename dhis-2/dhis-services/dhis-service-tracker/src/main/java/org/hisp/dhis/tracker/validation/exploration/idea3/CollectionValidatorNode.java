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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.bundle.TrackerBundle;

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

    public void visit( TrackerBundle bundle, Collection<? extends T> input, Consumer<Optional<Error>> consumer )
    {
        visit( this, bundle, input, consumer );
    }

    // TODO is this map or rather visit? I think for map it needs to take a
    // Function<T, S> so be more generic
    public void visit( CollectionValidatorNode<T> root, TrackerBundle bundle, Collection<? extends T> input,
        Consumer<Optional<Error>> consumer )
    {
        if ( root == null )
        {
            return;
        }

        // TODO refactor visit so that what we actually produce is a tree!
        // maybe then adapt it to accept a consumer as well?

        // map over input apply validator and create a node
        // collect all the nodes into children of a Node<Optional<Error>>
        List<Optional<Error>> errs = input.stream().map( i -> {
            Optional<Error> optionalError = root.validator.apply( bundle, i );
            consumer.accept( optionalError );
            return optionalError;
        } ).collect( Collectors.toList() );

        // TODO there might be value in visiting all nodes or only the behavior
        // of valid roots children
        // lets make it work for the default

        // only visit children of valid parents
        Optional<Error> optionalError = errs.stream().filter( Optional::isPresent ).findFirst()
            .orElse( Optional.empty() );
        if ( optionalError.isPresent() )
        {
            return;
        }

        for ( CollectionValidatorNode<T> child : root.children )
        {
            visit( child, bundle, input, consumer );
        }
    }

    public Node<Optional<Error>> apply( TrackerBundle bundle, Collection<? extends T> input )
    {
        ErrorNode result = new ErrorNode();

        this.visit( this, bundle, input, opt -> result.add( new ErrorNode( opt ) ) );

        return result;
    }

    // TODO this is just a helper for testing right now. Not sure yet how this
    // should look like
    // also since this is likely where fail fast will come into play as well
    public List<Error> validate( TrackerBundle bundle, Collection<? extends T> input )
    {
        List<Error> errs = new ArrayList<>();
        this.visit( bundle, input, o -> o.ifPresent( errs::add ) );
        return errs;
    }
}
