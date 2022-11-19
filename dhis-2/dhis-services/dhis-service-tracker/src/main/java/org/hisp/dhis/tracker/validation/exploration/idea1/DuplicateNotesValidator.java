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
package org.hisp.dhis.tracker.validation.exploration.idea1;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.validation.hooks.EnrollmentNoteValidationHook;
import org.hisp.dhis.tracker.validation.hooks.ValidationUtils;

/**
 * Would replace {@link EnrollmentNoteValidationHook} specifically
 * {@link ValidationUtils.validateNotes} as it's not concerned with Enrollments
 * itself.
 */
public class DuplicateNotesValidator implements Validator<List<Note>, TrackerErrorCode>
{

    @Override
    public Optional<TrackerErrorCode> apply( TrackerBundle bundle, List<Note> input )
    {
        final List<Note> duplicates = new ArrayList<>();
        for ( Note note : input )
        {
            if ( isNotEmpty( note.getValue() ) ) // Ignore notes with no text
            {
                // If a note having the same UID already exist in the db, raise
                // warning, ignore the note and continue
                if ( isNotEmpty( note.getNote() ) && bundle.getPreheat().getNote( note.getNote() ).isPresent() )
                {
                    duplicates.add( note );
                }
            }
        }

        if ( duplicates.isEmpty() )
        {
            return Optional.empty();
        }

        // TODO in the original validation we return a TrackerWarning; we could
        // create a sum type of TrackerError/Warning
        // so a validation can return either
        return Optional.of( TrackerErrorCode.E1119 );
    }
}
