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

import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1025;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1048;
import static org.hisp.dhis.tracker.report.TrackerErrorCode.E1122;
import static org.hisp.dhis.tracker.validation.exploration.idea3.Error.error;
import static org.hisp.dhis.tracker.validation.exploration.idea3.ValidatorTree.validate;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Note;

/**
 * Example class of how we would build our {@link Validator}s specific to our
 * domain types like {@link Enrollment}. Such higher level Validators can be
 * built using existing {@link Predicate}s or lambdas. More complex Validators
 * like PreCheckSecurityOwnershipValidationHook can live in separate classes.
 *
 * Validators can also be "grouped" which can be useful in creating reusable
 * groups in case we want to always run certain Validators no matter if it's a
 * create or update while others should only run on create.
 */
public class EnrollmentValidator
{
    public static ValidatorTree<Enrollment> uidProperties()
    {
        return new ValidatorTree<Enrollment>()
            .andThen( Enrollment::getEnrollment, CodeGenerator::isValidUid, error( E1048 ) ); // PreCheckUidValidationHook
        // TODO I have an idea of how to make this work
        // It might be best though to first think about how invalid notes could
        // be tagged/collected at the end of the validation
        // first
        // .validateEach( Enrollment::getNotes, Note::getNote,
        // CodeGenerator::isValidUid, error( E1048 ) ); //
        // PreCheckUidValidationHook
    }

    public static ValidatorTree<Note> notes()
    {
        // TODO attach to the enrollmentValidator() once I get the
        // CollectionValidator working
        return new ValidatorTree<Note>();
        // .validateEach( Enrollment::getNotes, Note::getNote,
        // CodeGenerator::isValidUid, error( E1048 ) ); //
        // PreCheckUidValidationHook
        // .andThen( Enrollment::getNotes, noDuplicateNotes() );

    }

    public static ValidatorTree<Enrollment> enrollmentValidator()
    {
        // TODO create a tree of validations; right now they are mostly run
        // independently
        return new ValidatorTree<Enrollment>()
            .andThen( uidProperties() )
            .andThen( e -> e.getOrgUnit().isNotBlank(), error( E1122, "orgUnit" ) ) // PreCheckMandatoryFieldsValidationHook
            .andThen(
                validate( Enrollment::getProgram, CommonValidations::notBlank, error( E1122, "program" ) )
                    .andThen( Enrollment::getProgram, CommonValidations::programInPreheat ) )// PreCheckMandatoryFieldsValidationHook
            // andThen
            // PreCheckMetaValidationHook
            .andThen( Enrollment::getTrackedEntity, StringUtils::isNotEmpty, error( E1122, "trackedEntity" ) ) // PreCheckMandatoryFieldsValidationHook
            .andThen( Enrollment::getEnrolledAt, Objects::nonNull, error( E1025, "null" ) ); // EnrollmentDateValidationHook.validateMandatoryDates
        // .andThen(Enrollment::getNotes, each(Node.class) );
    }
}
