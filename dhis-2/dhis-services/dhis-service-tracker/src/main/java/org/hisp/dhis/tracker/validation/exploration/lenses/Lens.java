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
package org.hisp.dhis.tracker.validation.exploration.lenses;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

// https://medium.com/expedia-group-tech/lenses-in-java-2b18c7d24366
// https://github.com/liquidpie/lenses-java/blob/main/src/main/java/com/vivek/pattern/Lens.java
public final class Lens<A, B>
{

    private final Function<A, B> getter;

    private final BiFunction<A, B, A> setter;

    public Lens( final Function<A, B> getter, final BiFunction<A, B, A> setter )
    {
        this.getter = getter;
        this.setter = setter;
    }

    public static <A, B> Lens<A, B> of( Function<A, B> getter, BiFunction<A, B, A> setter )
    {
        return new Lens<>( getter, setter );
    }

    public B get( final A a )
    {
        return getter.apply( a );
    }

    public A set( final A a, final B b )
    {
        return setter.apply( a, b );
    }

    public A mod( final A a, final UnaryOperator<B> unaryOperator )
    {
        return set( a, unaryOperator.apply( get( a ) ) );
    }

    public <C> Lens<C, B> compose( final Lens<C, A> that )
    {
        return new Lens<>(
            c -> get( that.get( c ) ),
            ( c, b ) -> that.mod( c, a -> set( a, b ) ) );
    }

    public <C> Lens<A, C> andThen( final Lens<B, C> that )
    {
        return that.compose( this );
    }

}
