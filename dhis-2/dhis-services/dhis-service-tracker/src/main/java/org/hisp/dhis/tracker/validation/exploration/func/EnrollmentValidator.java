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
import static org.hisp.dhis.tracker.validation.exploration.func.DuplicateNotesValidator.notBeADuplicate;
import static org.hisp.dhis.tracker.validation.exploration.func.Each.each;
import static org.hisp.dhis.tracker.validation.exploration.func.Field.field;
import static org.hisp.dhis.tracker.validation.exploration.func.Must.must;
import static org.hisp.dhis.tracker.validation.exploration.func.Seq.seq;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.Note;

public class EnrollmentValidator
{
    public static Validator<Enrollment> enrollmentValidator()
    {
        return all( Enrollment.class,
            containValidUids(),
            must( e -> e.getOrgUnit().isNotBlank(), "E1122" ), // PreCheckMandatoryFieldsValidationHook
            field( Enrollment::getTrackedEntity, StringUtils::isNotEmpty, "E1122" ), // PreCheckMetaValidationHook
            field( Enrollment::getProgram,
                seq( MetadataIdentifier.class,
                    must( CommonValidations::notBeBlank, "E1122" ), // PreCheckMandatoryFieldsValidationHook
                    must( CommonValidations::beInPreheat, "E1069" ) // PreCheckMetaValidationHook
                ) ),
            field( Enrollment::getEnrolledAt, Objects::nonNull, "E1025" ), // EnrollmentDateValidationHook.validateMandatoryDates
            field( Enrollment::getNotes,
                each( Note.class,
                    noteValidator() ) ) );
    }

    public static Validator<Enrollment> containValidUids()
    {
        // just an example showing that we can group validators into reusable
        // pieces
        // think of some Validators that always need to run irrespective of
        // insert/update/delete
        return all( Enrollment.class,
            field( Enrollment::getEnrollment, CodeGenerator::isValidUid, "E1048" ) // PreCheckUidValidationHook
        );
    }

    public static Validator<Note> noteValidator()
    {
        return all( Note.class,
            field( Note::getNote, CodeGenerator::isValidUid, "E1048" ), // PreCheckUidValidationHook
            notBeADuplicate() );
    }
}
