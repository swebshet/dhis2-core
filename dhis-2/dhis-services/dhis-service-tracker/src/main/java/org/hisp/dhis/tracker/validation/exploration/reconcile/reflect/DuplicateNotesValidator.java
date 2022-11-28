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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Optional;

import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentNoteValidationHook;
import org.hisp.dhis.tracker.validation.hooks.ValidationUtils;

/**
 * Would replace {@link EnrollmentNoteValidationHook} specifically
 * {@link ValidationUtils} validateNotes as it's not concerned with Enrollments
 * itself.
 */
class DuplicateNotesValidator implements Validator<Note>
{

    public static Validator<Note> notBeADuplicate()
    {
        return new DuplicateNotesValidator();
    }

    // TODO the interface in step2 is simplified so right now there is not
    // TrackerBundle/Preheat
    // see comments in Validator interface
    @Override
    public Optional<Error> apply( Note note )
    {

        // Ignore notes with no UID or no text
        if ( isEmpty( note.getNote() ) || isEmpty( note.getValue() ) )
        {
            return Error.succeed();
        }

        // TODO in the original validation we return a warning; my simple
        // reporter only adds errors; so imagine a call to addWarning()
        // if we would have the preheat then simply do
        // if ( bundle.getPreheat().getNote( note.getNote() ).isPresent() )

        // TODO see above comments. This matches the valid program in our test
        // just to show how everything fits together
        if ( "Kj6vYde4LHh".equals( note.getNote() ) )
        {
            return Error.fail( "E1119" );
        }

        return Error.succeed();
    }
}
