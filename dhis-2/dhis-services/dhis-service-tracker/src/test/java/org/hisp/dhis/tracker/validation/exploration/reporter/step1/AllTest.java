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
package org.hisp.dhis.tracker.validation.exploration.reporter.step1;

import static org.hisp.dhis.tracker.validation.exploration.reporter.step1.All.all;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllTest
{

    private static final MetadataIdentifier PROGRAM_ID = MetadataIdentifier.ofUid( "MNWZ6hnuhSw" );

    private TrackerPreheat preheat;

    private TrackerBundle bundle;

    private TrackerIdSchemeParams idSchemes;

    @BeforeEach
    void setUp()
    {

        idSchemes = TrackerIdSchemeParams.builder()
            .build();

        preheat = mock( TrackerPreheat.class );
        when( preheat.getIdSchemes() ).thenReturn( idSchemes );

        Program program = new Program();
        when( preheat.get( Program.class, PROGRAM_ID ) ).thenReturn( program );

        bundle = TrackerBundle.builder()
            .preheat( preheat )
            .build();

    }

    @Test
    void testAllAreCalled()
    {

        Enrollment enrollment = enrollment();

        Validator<Enrollment> validator = all(
            ( r, e ) -> r.add( "one" ),
            ( r, e ) -> r.add( "two" ) );

        ErrorReporter reporter = new ErrorReporter();

        validator.apply( reporter, enrollment );

        assertEquals( List.of( "one", "two" ), reporter.getErrors() );
    }

    @Test
    void testAllNested()
    {

        Enrollment enrollment = enrollment();

        Validator<Enrollment> validator = all(
            ( r, e ) -> r.add( "one" ),
            ( r, e ) -> r.add( "two" ),
            all(
                ( r, e ) -> r.add( "three" ),
                ( r, e ) -> r.add( "four" ),
                all(
                    ( r, e ) -> r.add( "five" ) ) ) );

        ErrorReporter reporter = new ErrorReporter();

        validator.apply( reporter, enrollment );

        assertEquals( List.of( "one", "two", "three", "four", "five" ), reporter.getErrors() );
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