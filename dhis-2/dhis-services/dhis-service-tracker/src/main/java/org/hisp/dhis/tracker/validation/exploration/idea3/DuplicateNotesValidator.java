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

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1119;
import static org.hisp.dhis.tracker.validation.exploration.idea3.Error.error;

import java.util.Optional;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentNoteValidationHook;
import org.hisp.dhis.tracker.validation.hooks.ValidationUtils;

/**
 * Would replace {@link EnrollmentNoteValidationHook} specifically
 * {@link ValidationUtils#validateNotes} as it's not concerned with Enrollments
 * itself.
 */
class DuplicateNotesValidator implements Validator<Note>
{

    public static Validator<Note> noDuplicateNotes()
    {
        return new DuplicateNotesValidator();
    }

    @Override
    public Optional<Error> apply( TrackerBundle bundle, Note note )
    {
        if ( isEmpty( note.getNote() ) || isEmpty( note.getValue() ) ) // Ignore
                                                                       // notes
                                                                       // with
                                                                       // no UID
                                                                       // or no
                                                                       // text
        {
            return Optional.empty();
        }

        // TODO in the original validation we return a TrackerWarning and ignore
        // the note as the validation itself is
        // mutating the notes.
        if ( bundle.getPreheat().getNote( note.getNote() ).isPresent() )
        {
            return Optional.of( error( bundle.getPreheat().getIdSchemes(), E1119, note.getNote() ) );
        }

        return Optional.empty();
    }
}
