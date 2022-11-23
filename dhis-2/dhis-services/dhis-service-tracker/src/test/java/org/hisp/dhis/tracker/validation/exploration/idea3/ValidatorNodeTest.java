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

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1000;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1080;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E9999;
import static org.hisp.dhis.tracker.validation.exploration.idea3.ValidatorNode.validate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;
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
    void testIndependentValidators()
    {

        ValidatorNode<Enrollment> root = validate( Enrollment.class )
            .andThen( validate( e -> Optional.of( error( E1048 ) ) ) )
            .andThen( validate( e -> Optional.of( error( E1000 ) ) ) );

        ErrorNode errNodes = root.apply( bundle, new Enrollment() );
        assertEquals( 2, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1000, E1048 ), errs );
    }

    @Test
    void testDependentValidatorsWillNotApplyToChildIfParentErrors()
    {

        ValidatorNode<Enrollment> root = new ValidatorNode<Enrollment>()
            .andThen(
                validate( (SimpleValidator<Enrollment>) e1 -> Optional.of( error( E1048 ) ) )
                    .andThen( validate( e2 -> Optional.of( error( E9999 ) ) ) ) )
            .andThen( validate( e -> Optional.of( error( E1080 ) ) ) );

        ErrorNode errNodes = root.apply( bundle, new Enrollment() );
        assertEquals( 2, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1080, E1048 ), errs );
    }

    @Test
    void testDependentValidatorsAppliesToChildIfParentPasses()
    {

        ValidatorNode<Enrollment> root = new ValidatorNode<Enrollment>()
            .andThen( validate( (SimpleValidator<Enrollment>) e1 -> Optional.empty() )
                .andThen( validate( e2 -> Optional.of( error( E9999 ) ) ) ) )
            .andThen( validate( e -> Optional.of( error( E1080 ) ) ) );

        ErrorNode errNodes = root.apply( bundle, new Enrollment() );
        assertEquals( 3, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1080, E9999 ), errs );
    }

    @Test
    void testValidatorOnCollection()
    {

        Enrollment enrollment = new Enrollment();
        List<Note> notes = List.of( Note.builder().note( "foo" ).build(), Note.builder().note( "faa" ).build() );
        enrollment.setNotes( notes );

        // T -> Collection<S>
        Function<Enrollment, List<Note>> getNotes = Enrollment::getNotes;
        // Validator<S>
        List<Optional<Error>> errs = getNotes.apply( enrollment ).stream().map( n -> {
            System.out.println( n.getNote() );
            return Optional.of( error( E1000 ) );
        } ).collect( Collectors.toList() );

        // ValidatorNode<Enrollment> root = new ValidatorNode<Enrollment>()
        // .andThen( each( Enrollment::getNotes, n -> {
        // System.out.println( n );
        // return Optional.of( error( E1000 ) );
        // } ) );

        // List<TrackerErrorCode> errs = new ArrayList<>();
        // root.visit( bundle, new Enrollment(), o -> o.ifPresent( e ->
        // errs.add( e.getCode() ) ) );
        //
        // assertContainsOnly( List.of( E1048, E1000 ), errs );
    }

    @Test
    void testValidatorOnCollectionTODO()
    {
        // TODO create a validator that given a Collection<T> it creates a
        // Node<Validator<T>>
        // where each child is a validation of an element in the collection

        List<Note> notes = List.of( Note.builder().note( "foo" ).build(), Note.builder().note( "faa" ).build() );

        // T -> Collection<S>
        Function<Enrollment, List<Note>> getNotes = Enrollment::getNotes;
        // Validator<S>

        CollectionValidatorNode<Note> root = new CollectionValidatorNode<>(
            ( __, n ) -> Optional.of( error( E1000, n.getNote() ) ) );

        ErrorNode errNodes = root.apply( bundle, notes );
        assertEquals( 2, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, notes, o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1000, E1000 ), errs );
    }

    private static Error error( TrackerErrorCode code )
    {
        return new Error( code, "" );
    }

    private static Error error( TrackerErrorCode code, String message )
    {
        return new Error( code, "" );
    }
}