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
package org.hisp.dhis.tracker.validation.exploration.func;

import static org.hisp.dhis.tracker.validation.exploration.func.All.all;
import static org.hisp.dhis.tracker.validation.exploration.func.Error.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllTest
{
    private static final MetadataIdentifier PROGRAM_ID = MetadataIdentifier.ofUid( "MNWZ6hnuhSw" );

    private Enrollment enrollment;

    @BeforeEach
    void setUp()
    {

        TrackerIdSchemeParams idSchemes = TrackerIdSchemeParams.builder()
            .build();

        TrackerPreheat preheat = mock( TrackerPreheat.class );
        when( preheat.getIdSchemes() ).thenReturn( idSchemes );

        Program program = new Program();
        when( preheat.get( Program.class, PROGRAM_ID ) ).thenReturn( program );

        enrollment = enrollment();
    }

    @Test
    void testAllAreCalled()
    {
        Validator<Enrollment> validator = all( Enrollment.class,
            e -> error( "one" ),
            e -> error( "two" ) );

        Optional<Error> error = validator.apply( enrollment );

        assertEquals( List.of( "one", "two" ), error.get().getErrors() );
        assertTrue( error.isPresent() );
    }

    @Test
    void testAllAreCalledPass()
    {
        Validator<Enrollment> validator = all( Enrollment.class,
            e -> Optional.empty(),
            e -> Optional.empty() );

        Optional<Error> error = validator.apply( enrollment );

        assertFalse( error.isPresent() );
    }

    @Test
    void testAllNested()
    {
        Validator<Enrollment> validator = all( Enrollment.class,
            e -> error( "one" ),
            e -> error( "two" ),
            all( Enrollment.class,
                e -> error( "three" ),
                e -> error( "four" ),
                all( Enrollment.class,
                    e -> error( "five" ) ) ) );

        Optional<Error> error = validator.apply( enrollment );

        assertEquals( List.of( "one", "two", "three", "four", "five" ), error.get().getErrors() );
        assertTrue( error.isPresent() );
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
}