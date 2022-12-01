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
package org.hisp.dhis.tracker.validation.next;

import java.util.List;
import java.util.function.BooleanSupplier;

import lombok.RequiredArgsConstructor;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.TrackerDto;
import org.hisp.dhis.tracker.report.Reporter;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.TrackerWarningReport;

/**
 * {@link Validator} applying a sequence of validators in order until a
 * {@link Validator} fails.
 *
 * NOTE: this Validator is currently not thread-safe!
 *
 * @param <T> type to validate
 */
@RequiredArgsConstructor
public class Seq<T> implements Validator<T>, Reporter
{
    private final List<Validator<T>> validators;

    private Reporter reporter;

    private boolean failed;

    private Seq( Validator<T> v1 )
    {
        this( List.of( v1 ) );
    }

    private Seq( Validator<T> v1, Validator<T> v2 )
    {
        this( List.of( v1, v2 ) );
    }

    private Seq( Validator<T> v1, Validator<T> v2, Validator<T> v3 )
    {
        this( List.of( v1, v2, v3 ) );
    }

    private Seq( Validator<T> v1, Validator<T> v2, Validator<T> v3, Validator<T> v4 )
    {
        this( List.of( v1, v2, v3, v4 ) );
    }

    public static <T> Seq<T> seq( Class<T> klass, Validator<T> v1 )
    {
        return new Seq<>( v1 );
    }

    public static <T> Seq<T> seq( Class<T> klass, Validator<T> v1, Validator<T> v2 )
    {
        return new Seq<>( v1, v2 );
    }

    public static <T> Seq<T> seq( Class<T> klass, Validator<T> v1, Validator<T> v2, Validator<T> v3 )
    {
        return new Seq<>( v1, v2, v3 );
    }

    public static <T> Seq<T> seq( Class<T> klass, Validator<T> v1, Validator<T> v2, Validator<T> v3, Validator<T> v4 )
    {
        return new Seq<>( v1, v2, v3, v4 );
    }

    public static <T> Seq<T> seq( Class<T> klass, List<Validator<T>> validators )
    {
        return new Seq<>( validators );
    }

    @Override
    public void apply( Reporter reporter, TrackerBundle bundle, T input )
    {
        this.reporter = reporter;
        for ( Validator<T> validator : validators )
        {
            // in order to get the signal that an error occurred we implement
            // the Reporter
            // and delegate to the reporter given to us
            // NOTE: rethink this solution once our transition to the new
            // Validators is done
            // this is not thread-safe!
            validator.apply( this, bundle, input );
            if ( this.failed )
            {
                // only apply next validator if previous one was successful
                return;
            }
        }
        this.reporter = null;
        this.failed = false;
    }

    @Override
    public void addError( TrackerDto dto, TrackerErrorCode code, Object... args )
    {
        this.failed = true;
        this.reporter.addError( dto, code, args );
    }

    @Override
    public void addError( TrackerErrorReport error )
    {
        this.failed = true;
        this.reporter.addError( error );
    }

    // TODO check if I can add default implementations for these helpers?
    @Override
    public void addErrorIf( BooleanSupplier expression, TrackerDto dto, TrackerErrorCode code, Object... args )
    {
        this.failed = true;
        this.reporter.addErrorIf( expression, dto, code, args );
    }

    @Override
    public void addErrorIfNull( Object object, TrackerDto dto, TrackerErrorCode code, Object... args )
    {
        this.failed = true;
        this.reporter.addErrorIfNull( object, dto, code, args );
    }

    @Override
    public void addWarning( TrackerWarningReport warning )
    {
        this.reporter.addWarning( warning );
    }

    @Override
    public void addWarning( TrackerDto dto, TrackerErrorCode code, Object... args )
    {
        this.reporter.addWarning( dto, code, args );
    }

    @Override
    public void addWarningIf( BooleanSupplier expression, TrackerDto dto, TrackerErrorCode code, Object... args )
    {
        this.reporter.addWarningIf( expression, dto, code, args );
    }
}
