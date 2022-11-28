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
package org.hisp.dhis.tracker.validation.exploration.reconcile.reflect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class Error
{
    // TODO please ignore this name :joy: I had time pressure
    @Getter
    @RequiredArgsConstructor
    static class AnnotatedError
    {
        private final String error;

        // private final List<Segment> path;
        private final List<String> path = new ArrayList<>();

        public void prependPath( String segment )
        {
            path.add( 0, segment );
        }
    }

    private final List<AnnotatedError> errors;

    // TODO paths need to be part of the List<String> String type
    // each error might be deeper into the hierarchy
    // private final List<Path> paths;

    // wonder if we should discourage creating an empty Error as we want to
    // indicate a lack of error with an empty
    // Optional
    private Error()
    {
        this.errors = new ArrayList<>();
    }

    public Error( String message )
    {
        this.errors = new ArrayList<>();
        // TODO should this be allowed?
        this.errors.add( new AnnotatedError( message ) );
    }

    public Error( String message, String path )
    {
        this.errors = new ArrayList<>();
        AnnotatedError e = new AnnotatedError( message );
        e.path.add( path );
        this.errors.add( e );
    }

    public Error append( Optional<Error> error, String path )
    {
        error.ifPresent( e -> append( e, path ) );
        return this;
    }

    // TODO prepend a path
    public Error append( Error error, String path )
    {
        List<AnnotatedError> errors1 = error.getErrors();
        errors1.forEach( e -> {
            if ( e.getPath() != null )
            {
                e.prependPath( path );
            }
        } );
        this.errors.addAll( errors1 );
        return this;
    }

    public void prependPath( String segment )
    {
        errors.forEach( e -> {
            if ( e.getPath() != null )
            {
                e.prependPath( segment );
            }
        } );
    }

    public static Optional<Error> fail( String message )
    {
        return Optional.of( new Error( message ) );
    }

    public static Optional<Error> fail( String message, String path )
    {
        return Optional.of( new Error( message, path ) );
    }

    public static Optional<Error> succeed()
    {
        return Optional.empty();
    }
}
