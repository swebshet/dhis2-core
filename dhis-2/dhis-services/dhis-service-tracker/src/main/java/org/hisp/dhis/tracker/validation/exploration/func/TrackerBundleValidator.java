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
import static org.hisp.dhis.tracker.validation.exploration.func.Each.each;
import static org.hisp.dhis.tracker.validation.exploration.func.EnrollmentValidator.enrollmentValidator;
import static org.hisp.dhis.tracker.validation.exploration.func.Field.field;

import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.Enrollment;

/**
 * Shows how we could even build a {@link Validator} for the entire payload from
 * Validators which only know how to validate a specific entity.
 *
 * NOTE: it would not entirely work just yet, just due to the fact that our
 * validations (errors) are flattened and have no link to the field/entity which
 * is invalid. We can explore some ideas on that as well :)
 */
public class TrackerBundleValidator
{

    public static Validator<TrackerBundle> validatorForCreate()
    {
        return all( TrackerBundle.class,
            field( TrackerBundle::getEnrollments,
                each( Enrollment.class,
                    enrollmentValidator() ) ) );
    }
}
