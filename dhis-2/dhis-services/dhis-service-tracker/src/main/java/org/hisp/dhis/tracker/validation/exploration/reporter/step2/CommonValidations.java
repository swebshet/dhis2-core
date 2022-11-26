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
package org.hisp.dhis.tracker.validation.exploration.reporter.step2;

import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.validation.exploration.initial.idea1.Validator;

/**
 * It's easy to create common {@link Validator}s or predicates that we can reuse
 * across our different entities.
 */
class CommonValidations
{

    public static boolean notBeBlank( MetadataIdentifier id )
    {
        if ( id == null )
        {
            return false;
        }

        return id.isNotBlank();
    }

    // TODO the interface in step2 is simplified so right now there is not
    // TrackerBundle/Preheat
    // this is easy to adjust. Imagine this function is getting the
    // TrackerBundle/Preheat
    // TrackerPreheat preheat = bundle.getPreheat();
    // public static boolean programInPreheat(TrackerBundle bundle,
    // ErrorReporter reporter, MetadataIdentifier id )
    public static boolean beInPreheat( MetadataIdentifier id )
    {

        // TODO rough example of how it could look like with access to the
        // preheat
        // If we find a way to make this more generic or fit in with the other
        // functions we could extract the
        // class. Its a common pattern to check for existance so a function like
        //
        // Program p = preheat.get( Program.class, id );
        // if ( p == null )
        // {
        // return error( preheat.getIdSchemes(), E1069, id );
        // }

        // TODO see above comments. This matches the valid program in our test
        // just to show how everything fits together
        if ( !id.equals( MetadataIdentifier.ofUid( "MNWZ6hnuhSw" ) ) )
        {
            return false;
        }

        return true;
    }
}
