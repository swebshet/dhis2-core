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
package org.hisp.dhis.tracker.validation.exploration.reconcile.reflect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;
import org.junit.jupiter.api.Test;

class FieldTest
{

    @Test
    void testFieldAddsLens()
        throws NoSuchFieldException,
        IllegalAccessException,
        NoSuchMethodException,
        InvocationTargetException
    {

        Enrollment enrollment = new Enrollment();
        List<Note> notes = List.of(
            note( "Kj6vYde4LHh", "my duplicate note" ),
            note( "olfXZzSGacW", "valid note" ),
            note( "invalid1", "note 1 with invalid uid" ),
            note( "invalid2", "note 2 with invalid uid" ) );
        enrollment.setNotes( notes );

        Method getNotes = Enrollment.class.getDeclaredMethod( "getNotes", null );
        Method getNote = Note.class.getDeclaredMethod( "getNote", null );
        List<Segment> path = List.of(
            new Segment( getNotes ),
            new Segment( List.class.getDeclaredMethod( "get" ), 2 ),
            new Segment( getNote ) );

        Method method1 = path.get( 0 ).getMethod();
        Object[] args1 = path.get( 0 ).getArgs();
        Object res1 = method1.invoke( enrollment, args1 );

        // TODO how can I call the method "get" on res1?

        Method method2 = path.get( 1 ).getMethod();
        Object[] args2 = path.get( 1 ).getArgs();
        method2.invoke( enrollment, args2 );

        // Error error = new Error("E1048", new Path(Note.class, "note"));

        Note invalid1 = note( "invalid1", "note 1 with invalid uid" );
        // List<Note> notes = List.of(invalid1);

        Field field = Note.class.getDeclaredField( "note" );
        Method method = Note.class.getDeclaredMethod( "getNote", null );
        // field is private
        // assertEquals("invalid1", field.get(invalid1));
        assertEquals( "invalid1", method.invoke( invalid1 ) );
    }

    private static Note note( String uid, String value )
    {
        return Note.builder().note( uid ).value( value ).build();
    }

}