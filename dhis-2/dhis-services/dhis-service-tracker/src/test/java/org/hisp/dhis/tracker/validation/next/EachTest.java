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

import static org.hisp.dhis.tracker.validation.next.Each.each;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.report.Reporter;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.report.ValidationErrorReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EachTest
{

    private ValidationErrorReporter reporter;

    private TrackerBundle bundle;

    @BeforeEach
    void setUp()
    {
        TrackerIdSchemeParams idSchemes = TrackerIdSchemeParams.builder().build();
        reporter = new ValidationErrorReporter( idSchemes );
        bundle = TrackerBundle.builder().build();
    }

    @Test
    void testCalledForEachElementInCollectionPasses()
    {
        List<Note> notes = List.of(
            note( "Kj6vYde4LHh", "note1" ),
            note( "olfXZzSGacW", "note2" ),
            note( "jKLB23QZS4I", "note3" ) );

        Validator<Collection<Note>> validator = each( Note.class,
            ( r, b, n ) -> {
            } // no error
        );

        validator.apply( reporter, bundle, notes );

        assertFalse( reporter.hasErrors() );
        assertFalse( reporter.hasWarnings() );
    }

    @Test
    void testCalledForEachElementInCollectionFails()
    {
        List<Note> notes = List.of(
            note( "Kj6vYde4LHh", "note1" ),
            note( "olfXZzSGacW", "note2" ),
            note( "jKLB23QZS4I", "note3" ) );

        Validator<Collection<Note>> validator = each( Note.class,
            ( r, b, n ) -> error( r, n ) );

        validator.apply( reporter, bundle, notes );

        assertContainsOnly( List.of( "Kj6vYde4LHh", "olfXZzSGacW", "jKLB23QZS4I" ),
            reporter.getReportList().stream().map( e -> e.getUid() ).collect( Collectors.toList() ) );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

    private static void error( Reporter reporter, Note note )
    {
        reporter.addError( TrackerErrorReport.builder().uid( note.getNote() ).build() );
    }
}