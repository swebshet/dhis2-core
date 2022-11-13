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
package org.hisp.dhis.tracker.validation.experiment;

import lombok.RequiredArgsConstructor;

// TODO it should implement an interface that Errors will also implement
// TODO switch order of T and E as this is in accordance with Either where errors are usually on the left
public interface Validation<T, E>
{

    boolean isSuccess();

    T get();

    E getFail();

    @RequiredArgsConstructor
    class Success<T, E> implements Validation<T, E>
    {

        private final T t;

        public static <T, E> Success<T, E> success( T t )
        {
            return new Success<>( t );
        }

        @Override
        public boolean isSuccess()
        {
            return true;
        }

        @Override
        public T get()
        {
            return t;
        }

        @Override
        public E getFail()
        {
            throw new IllegalStateException();
        }
    }

    @RequiredArgsConstructor
    class Fail<T, E> implements Validation<T, E>
    {

        private final E e;

        public static <T, E> Fail<T, E> fail( E e )
        {
            return new Fail<>( e );
        }

        @Override
        public boolean isSuccess()
        {
            return false;
        }

        @Override
        public T get()
        {
            throw new IllegalStateException();
        }

        @Override
        public E getFail()
        {
            return e;
        }
    }
}
