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
package org.hisp.dhis.tracker.validation.exploration.reporter.step2;

import static org.hisp.dhis.tracker.validation.exploration.reporter.step2.All.all;
import static org.hisp.dhis.tracker.validation.exploration.reporter.step2.Must.must;
import static org.hisp.dhis.tracker.validation.exploration.reporter.step2.Seq.seq;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.domain.Enrollment;

public class EnrollmentValidator
{
    public static Validator<Enrollment> uidProperties()
    {
        return all( Enrollment.class,
            must( Enrollment::getEnrollment, CodeGenerator::isValidUid, "E1048" ) // PreCheckUidValidationHook
        );
        // TODO I have an idea of how to make this work
        // It might be best though to first think about how invalid notes could
        // be tagged/collected at the end of the validation
        // first
        // .validateEach( Enrollment::getNotes, Note::getNote,
        // CodeGenerator::isValidUid, error( E1048 ) ); //
        // PreCheckUidValidationHook
    }

    public static Validator<Enrollment> enrollmentValidator()
    {
        return all( Enrollment.class,
            uidProperties(),
            must( e -> e.getOrgUnit().isNotBlank(), "E1122" ), // PreCheckMandatoryFieldsValidationHook
            must( Enrollment::getTrackedEntity, StringUtils::isNotEmpty, "E1122" ), // PreCheckMetaValidationHook
            seq( Enrollment.class,
                must( Enrollment::getProgram, CommonValidations::notBeBlank, "E1122" ), // PreCheckMandatoryFieldsValidationHook
                must( Enrollment::getProgram, CommonValidations::beInPreheat, "E1069" ) // PreCheckMetaValidationHook
            ),
            must( Enrollment::getEnrolledAt, Objects::nonNull, "E1025" ) // EnrollmentDateValidationHook.validateMandatoryDates
        );
        // PreCheckMandatoryFieldsValidationHook
        // // .andThen(Enrollment::getNotes, each(Node.class) );
    }
}
