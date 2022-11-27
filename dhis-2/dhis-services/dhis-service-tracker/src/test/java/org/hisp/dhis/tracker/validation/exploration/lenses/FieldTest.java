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

import static org.hisp.dhis.tracker.validation.exploration.lenses.Error.fail;
import static org.hisp.dhis.tracker.validation.exploration.lenses.Field.field;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;
import org.junit.jupiter.api.Test;

public class FieldTest
{

    @Test
    void testField()
    {

        Enrollment enrollment = new Enrollment();
        enrollment.setTrackedEntity( "PuBvJxDB73z" );

        org.hisp.dhis.tracker.validation.exploration.lenses.Validator<String> isValidUid = uid -> {
            return fail( uid ); // to demonstrate that we are getting the
                                // trackedEntity field
        };

        Validator<Enrollment> validator = field(
            Enrollment::getTrackedEntity,
            isValidUid );

        Optional<Error> error = validator.apply( enrollment );

        assertTrue( error.isPresent() );
        assertEquals( List.of( "PuBvJxDB73z" ), error.get().getErrors() );
    }

    @Test
    void testFieldAddsLens()
    {

        Note invalid1 = note( "invalid1", "note 1 with invalid uid" );

        Validator<Note> noteValidator = field( Note::getNote, CodeGenerator::isValidUid, "E1048" );

        Optional<Error> error = noteValidator.apply( invalid1 );

        assertTrue( error.isPresent() );
        assertContainsOnly( List.of( "E1048" ), error.get().getErrors() );
        // assertEquals( "invalid1",
        // error.get().getPaths().get(0).get(invalid1));
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

}
