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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hisp.dhis.tracker.report.TrackerErrorCode;

// TODO adapt to result vs the other class being validation
// I think my confusion comes also a little bit from the names
// this is the semi-group collecting the errors that is part of the sum type Validation
public class Errors
{
    // TODO implement the short-circuiting behavior we need for FAIL_FAST by
    // making it a monad and a semi-group

    private final Set<TrackerErrorCode> errors;

    public Errors()
    {
        this.errors = new HashSet<>();
    }

    public Errors( Set<TrackerErrorCode> errors )
    {
        this.errors = errors;
    }

    public Errors( TrackerErrorCode error )
    {
        this.errors = Collections.singleton( error );
    }

    public Set<TrackerErrorCode> getErrors()
    {
        return errors;
    }

    // this is what makes it a semi-group; "binary" if you count itself as an
    // arg operation that is associative
    public Errors append( Errors other )
    {

        Set<TrackerErrorCode> joined = Stream.concat( errors.stream(), other.getErrors().stream() )
            .collect( Collectors.toSet() );
        return new Errors( joined );
    }
}
