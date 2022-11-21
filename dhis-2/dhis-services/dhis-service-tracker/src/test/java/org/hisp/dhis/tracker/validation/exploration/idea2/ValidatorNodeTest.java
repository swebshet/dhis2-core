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
package org.hisp.dhis.tracker.validation.exploration.idea2;

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1000;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1080;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E9999;
import static org.hisp.dhis.tracker.validation.exploration.idea2.ValidatorNode.validate;
import static org.hisp.dhis.utils.Assertions.assertContainsOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidatorNodeTest
{

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

        bundle = TrackerBundle.builder()
            .preheat( preheat )
            .build();
    }

    @Test
    void testFunctions()
    {

        // TODO add into the IDEA markdown
        // everything is just a transformation
        // Enrollment -> String -> Predicate<String> -> boolean ->
        // Optional<Error>
        // Enrollment::getEnrollment -> CodeGenerator::isValidUid

        // let's first do the work on the client of providing a Function<T,
        // Optional<Error>
        // later I can add nice helpers that allow passing individual functions
        // so client code is more similar to IDEA1

        // TODO work on a root later, lets get a simple validation going
        // ValidatorNode<Enrollment> root = node() // acts as a ValidatorNode
        // with Function.identity()
        // .validate();
        // ValidatorNode<Enrollment> root = node(validator);

        // Enrollment::getEnrollment, CodeGenerator::isValidUid, error( E1048 )
        Function<Boolean, Optional<Error>> predicateToError = b -> {
            if ( !b )
            {
                return Optional.of( error( E1048 ) );
            }
            return Optional.empty();
        };
        // One type of Validator
        Function<Enrollment, Optional<Error>> validator = ((Function<Enrollment, String>) Enrollment::getEnrollment) // optional
                                                                                                                     // transformation
            .andThen( CodeGenerator::isValidUid ) // predicate
            .andThen( predicateToError ); // map result of predicate (boolean)
                                          // to Optional<Error>

        Enrollment enrollment = new Enrollment();
        // validation error: invalid UID
        enrollment.setEnrollment( "invalid" );

        Optional<Error> result = validator.apply( enrollment );

        assertTrue( result.isPresent() );
        assertEquals( E1048, result.get().getCode() );
    }

    @Test
    void testIndependentValidators()
    {

        ValidatorNode<Enrollment> root = new ValidatorNode<Enrollment>()
            .andThen( validate( e -> Optional.of( error( E1048 ) ) ) )
            .andThen( validate( e -> Optional.of( error( E1000 ) ) ) );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.visit( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );

        assertContainsOnly( List.of( E1048, E1000 ), errs );
    }

    @Test
    void testDependentValidators()
    {

        ValidatorNode<Enrollment> root = new ValidatorNode<Enrollment>()
            .andThen(
                validate( (SimpleValidator<Enrollment>) e1 -> Optional.of( error( E1048 ) ) )
                    .andThen( validate( e2 -> Optional.of( error( E9999 ) ) ) ) )
            .andThen( validate( e -> Optional.of( error( E1080 ) ) ) );

        // TODO is it map or actually apply? I mean every node is a function
        // which maps
        // and what can one do with this :thinking:
        Node<Optional<Error>> result = root.apply( bundle, new Enrollment() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.visit( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );

        assertContainsOnly( List.of( E1048, E1080 ), errs );
    }

    private static Error error( TrackerErrorCode code )
    {
        return new Error( code, "" );
    }
}