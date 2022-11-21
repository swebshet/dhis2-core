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
import java.util.function.Consumer;
import java.util.function.Function;

// TODO generalize this; to get to my idea of the tree.
// If some nodes func are only transformations than the children need to match the return type R of the func of their
// parent.
// if the node itself
public class ValidatorNode<T> implements Node<Function<T, Optional<Error>>>
{

    private final Function<T, Optional<Error>> func;

    private final List<ValidatorNode<T>> children = new ArrayList<>();

    public ValidatorNode()
    {
        this.func = t -> Optional.empty();
    }

    public ValidatorNode( Function<T, Optional<Error>> func )
    {
        this.func = func;
    }

    // TODO this does not work without casting hell
    public static <T> ValidatorNode<T> validate()
    {
        return new ValidatorNode<T>( t -> Optional.empty() );
    }

    public static <T> ValidatorNode<T> validate( Function<T, Optional<Error>> func )
    {
        return new ValidatorNode<T>( func );
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

    public Node<Optional<Error>> map( T input )
    {
        ErrorNode result = new ErrorNode();

        visit( this, input, opt -> result.add( new ErrorNode( opt ) ) );

        return result;
    }

    public void visit( T input, Consumer<Optional<Error>> consumer )
    {
        visit( this, input, consumer );
    }

    // TODO is this map or rather visit?
    public void visit( ValidatorNode<T> root, T input, Consumer<Optional<Error>> consumer )
    {
        if ( root == null )
        {
            return;
        }

        Optional<Error> optionalError = root.func.apply( input );
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
            visit( child, input, consumer );
        }
    }

    @Override
    public Function<T, Optional<Error>> get()
    {
        return func;
    }
}
