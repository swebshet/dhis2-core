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
package org.hisp.dhis.tracker.validation.hooks;

import static org.hisp.dhis.tracker.TrackerType.TRACKED_ENTITY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.tracker.TrackerIdSchemeParams;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.ValidationMode;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.report.TrackerErrorCode;
import org.hisp.dhis.tracker.validation.ValidationErrorReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * @author Enrico Colasante
 */
@MockitoSettings( strictness = Strictness.LENIENT )
@ExtendWith( MockitoExtension.class )
class TrackedEntityPreCheckMandatoryFieldsValidatorTest
{

    private TrackedEntityPreCheckMandatoryFieldsValidator validator;

    @Mock
    private TrackerBundle bundle;

    @Mock
    private TrackerPreheat preheat;

    private ValidationErrorReporter reporter;

    @BeforeEach
    public void setUp()
    {
        validator = new TrackedEntityPreCheckMandatoryFieldsValidator();

        when( bundle.getImportStrategy() ).thenReturn( TrackerImportStrategy.CREATE_AND_UPDATE );
        when( bundle.getValidationMode() ).thenReturn( ValidationMode.FULL );
        when( bundle.getPreheat() ).thenReturn( preheat );

        TrackerIdSchemeParams idSchemes = TrackerIdSchemeParams.builder().build();
        reporter = new ValidationErrorReporter( idSchemes );
    }

    @Test
    void verifyTrackedEntityValidationSuccess()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( CodeGenerator.generateUid() )
            .trackedEntityType( MetadataIdentifier.ofUid( CodeGenerator.generateUid() ) )
            .orgUnit( MetadataIdentifier.ofUid( CodeGenerator.generateUid() ) )
            .build();

        validator.validate( reporter, bundle, trackedEntity );

        assertFalse( reporter.hasErrors() );
    }

    @Test
    void verifyTrackedEntityValidationFailsOnMissingOrgUnit()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( CodeGenerator.generateUid() )
            .trackedEntityType( MetadataIdentifier.ofUid( CodeGenerator.generateUid() ) )
            .orgUnit( MetadataIdentifier.EMPTY_UID )
            .build();

        validator.validate( reporter, bundle, trackedEntity );

        assertMissingProperty( reporter, trackedEntity.getUid(), "orgUnit" );
    }

    @Test
    void verifyTrackedEntityValidationFailsOnMissingTrackedEntityType()
    {
        TrackedEntity trackedEntity = TrackedEntity.builder()
            .trackedEntity( CodeGenerator.generateUid() )
            .trackedEntityType( MetadataIdentifier.EMPTY_UID )
            .orgUnit( MetadataIdentifier.ofUid( CodeGenerator.generateUid() ) )
            .build();

        validator.validate( reporter, bundle, trackedEntity );

        assertMissingProperty( reporter, trackedEntity.getUid(), "trackedEntityType" );
    }

    private void assertMissingProperty( ValidationErrorReporter reporter, String uid, String property )
    {
        AssertValidationErrorReporter.assertMissingProperty( reporter, TRACKED_ENTITY, "tracked entity", uid, property,
            TrackerErrorCode.E1121 );
    }
}