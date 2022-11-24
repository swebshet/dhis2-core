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
import static org.hisp.dhis.tracker.validation.exploration.idea3.ValidatorTree.validate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidatorTreeTest
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

        ValidatorTree<Enrollment> root = validate( Enrollment.class )
            .and( validate( e -> Optional.of( error( E1048 ) ) ) )
            .and( validate( e -> Optional.of( error( E1000 ) ) ) );

        Node<Optional<Error>> errNodes = root.apply( bundle, new Enrollment() );

        assertEquals( 2, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1000, E1048 ), errs );
    }

    @Test
    void testDependentValidatorsWillNotApplyToChildIfParentErrors()
    {

        ValidatorTree<Enrollment> root = new ValidatorTree<Enrollment>()
            .and(
                validate( (SimpleValidator<Enrollment>) e1 -> Optional.of( error( E1048 ) ) )
                    .and( validate( e2 -> Optional.of( error( E9999 ) ) ) ) )
            .and( validate( e -> Optional.of( error( E1080 ) ) ) );

        Node<Optional<Error>> errNodes = root.apply( bundle, new Enrollment() );

        assertEquals( 2, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1080, E1048 ), errs );
    }

    @Test
    void testDependentValidatorsAppliesToChildIfParentPasses()
    {

        ValidatorTree<Enrollment> root = new ValidatorTree<Enrollment>()
            .and( validate( (SimpleValidator<Enrollment>) e1 -> Optional.empty() )
                .and( validate( e2 -> Optional.of( error( E9999 ) ) ) ) )
            .and( validate( e -> Optional.of( error( E1080 ) ) ) );

        Node<Optional<Error>> errNodes = root.apply( bundle, new Enrollment() );

        // the parent of E9999 is an empty ErrorNode
        assertEquals( 3, errNodes.getChildren().size() );

        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, new Enrollment(), o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1080, E9999 ), errs );
    }

    @Test
    void testCollectionValidator()
    {
        List<Note> notes = List.of(
            note( "Kj6vYde4LHh", "note 1" ),
            note( "olfXZzSGacW", "note 2" ) );

        CollectionValidatorNode<Note> root = new CollectionValidatorNode<>(
            ( __, n ) -> Optional.of( error( E1000, n.getNote() ) ) );

        Node<Optional<Error>> errNodes = root.apply( bundle, notes );

        assertEquals( 2, errNodes.getChildren().size() );
        List<TrackerErrorCode> errs = new ArrayList<>();
        root.apply( bundle, notes, o -> o.ifPresent( e -> errs.add( e.getCode() ) ) );
        assertEquals( List.of( E1000, E1000 ), errs );
    }

    // TODO how to connect the CollectionValidatorNode with the ValidatorTree?
    @Test
    void testConnectingCollectionValidatorWithValidatorNode()
    {

        Enrollment enrollment = new Enrollment();
        List<Note> notes = List.of(
            note( "Kj6vYde4LHh", "note 1" ),
            note( "olfXZzSGacW", "note 2" ) );
        enrollment.setNotes( notes );

        CollectionValidatorNode<Note> noteValidator = new CollectionValidatorNode<>(
            ( __, n ) -> {
                return Optional.of( error( E1000, n.getNote() ) );
            } );

        // T -> Collection<S>
        // Validator<S>
        ValidatorTree<Enrollment> root = new ValidatorTree<Enrollment>()
            .and( Enrollment::getNotes, noteValidator );

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

    private static Error error( TrackerErrorCode code )
    {
        return new Error( code, "" );
    }

    private static Error error( TrackerErrorCode code, String message )
    {
        return new Error( code, "" );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }
}