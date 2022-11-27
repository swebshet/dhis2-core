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

import static org.hisp.dhis.tracker.validation.exploration.lenses.All.all;
import static org.hisp.dhis.tracker.validation.exploration.lenses.Each.each;
import static org.hisp.dhis.tracker.validation.exploration.lenses.Field.field;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.Note;
import org.junit.jupiter.api.Test;

class LensTest
{
    private static final MetadataIdentifier PROGRAM_ID = MetadataIdentifier.ofUid( "MNWZ6hnuhSw" );

    @Test
    void testGetterOfSimpleLens()
    {

        Enrollment enrollment = enrollment();
        enrollment.setNotes( List.of(
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "olfXZzSGacW", "valid note" ),
            note( "invalid1", "note 1 with invalid uid" ),
            note( "invalid2", "note 2 with invalid uid" ) ) );

        // ignoring getters for now
        Lens<Enrollment, MetadataIdentifier> enrollmentToProgram = Lens.of( Enrollment::getProgram, ( e, id ) -> e );
        Lens<MetadataIdentifier, String> programToIdentifier = Lens.of( MetadataIdentifier::getIdentifier,
            ( id, s ) -> id );
        Lens<Enrollment, String> enrollmentToProgramIdentifier = programToIdentifier.compose( enrollmentToProgram );

        assertEquals( "MNWZ6hnuhSw", enrollmentToProgramIdentifier.get( enrollment ) );
    }

    @Test
    void testGetterOfCollectionLens()
    {

        Enrollment enrollment = enrollment();
        enrollment.setNotes( List.of(
            note( "olfXZzSGacW", "valid note" ),
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "invalid1", "note 1 with invalid uid" ),
            note( "invalid2", "note 2 with invalid uid" ) ) );

        Lens<Enrollment, List<Note>> enrollmentToNotes = Lens.of( Enrollment::getNotes, ( e, n ) -> e );
        Lens<List<Note>, Note> notesToSecondNote = Lens.of( notes -> notes.get( 1 ), ( e, n ) -> e );
        Lens<Enrollment, Note> enrollmentToSecondNote = notesToSecondNote.compose( enrollmentToNotes );

        assertEquals( "Kj6vYde4LHh", enrollmentToSecondNote.get( enrollment ).getNote() );
    }

    @Test
    void testLensToGetInvalidNotesFromEnrollment()
    {
        Enrollment enrollment = enrollment();
        Note invalid1 = note("invalid1", "note 1 with invalid uid");
        enrollment.setNotes( List.of(
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "olfXZzSGacW", "valid note" ),
                invalid1,
            note( "invalid2", "note 2 with invalid uid" ) ) );

        Optional<Error> error = noteValidator().apply( invalid1 );
        // TODO end goal is validating a top level thing and getting a lens into the deeply nested thing where to error
        // originated
//        Optional<Error> error = enrollmentValidator().apply( enrollment );

        assertTrue( error.isPresent() );
        assertContainsOnly( List.of( "E1048" ), error.get().getErrors() );
    }

    private static Validator<Enrollment> enrollmentValidator()
    {
        return all( Enrollment.class,
            field( Enrollment::getNotes,
                each( Note.class, noteValidator() ) ) );
    }

    private static Validator<Note> noteValidator()
    {
        return all( Note.class,
            field( Note::getNote, CodeGenerator::isValidUid, "E1048" ) // PreCheckUidValidationHook
        // notBeADuplicate()
        );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

    private static Enrollment enrollment()
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollment( CodeGenerator.generateUid() );
        enrollment.setProgram( PROGRAM_ID );
        enrollment.setOrgUnit( MetadataIdentifier.ofUid( "Nav6inZRw1u" ) );
        enrollment.setTrackedEntity( "PuBvJxDB73z" );
        enrollment.setEnrolledAt( Instant.now() );
        return enrollment;
    }

    private static MetadataIdentifier uid()
    {
        return MetadataIdentifier.ofUid( CodeGenerator.generateUid() );
    }
}