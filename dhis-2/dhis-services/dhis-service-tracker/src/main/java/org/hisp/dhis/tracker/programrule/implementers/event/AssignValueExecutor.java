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
package org.hisp.dhis.tracker.programrule.implementers.event;

import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.math.NumberUtils;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.DataValue;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.MetadataIdentifier;
import org.hisp.dhis.tracker.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.programrule.IssueType;
import org.hisp.dhis.tracker.programrule.ProgramRuleIssue;
import org.hisp.dhis.tracker.programrule.implementers.RuleActionExecutor;
import org.hisp.dhis.tracker.validation.ValidationCode;

import com.google.common.collect.Lists;

/**
 * This executor assigns a value to a field if it is empty, otherwise returns an
 * error
 *
 * @Author Enrico Colasante
 */
@RequiredArgsConstructor
public class AssignValueExecutor implements RuleActionExecutor<Event>
{
    private final SystemSettingManager systemSettingManager;

    private final String ruleUid;

    private final String value;

    private final String fieldUid;

    private final Set<DataValue> dataValues;

    @Override
    public String getField()
    {
        return fieldUid;
    }

    @Override
    public Optional<ProgramRuleIssue> executeRuleAction( TrackerBundle bundle, Event event )
    {
        Boolean canOverwrite = systemSettingManager
            .getBooleanSetting( SettingKey.RULE_ENGINE_ASSIGN_OVERWRITE );

        TrackerPreheat preheat = bundle.getPreheat();

        DataElement dataElement = bundle.getPreheat().getDataElement( fieldUid );

        Optional<DataValue> payloadDataValue = dataValues.stream()
            .filter( dv -> dv.getDataElement().isEqualTo( dataElement ) )
            .findAny();

        if ( payloadDataValue.isEmpty() ||
            Boolean.TRUE.equals( canOverwrite ) ||
            isEqual( value, payloadDataValue.get().getValue(), dataElement.getValueType() ) )
        {
            addOrOverwriteDataValue( event, bundle );
            return Optional.of( new ProgramRuleIssue( ruleUid, ValidationCode.E1308,
                Lists.newArrayList( fieldUid, event.getEvent() ), IssueType.WARNING ) );
        }
        else
        {
            return Optional.of( new ProgramRuleIssue( ruleUid, ValidationCode.E1307,
                Lists.newArrayList( fieldUid, value ), IssueType.ERROR ) );
        }
    }

    private void addOrOverwriteDataValue( Event event, TrackerBundle bundle )
    {
        DataElement dataElement = bundle.getPreheat().getDataElement( fieldUid );

        Optional<DataValue> dataValue = event.getDataValues().stream()
            .filter( dv -> dv.getDataElement().isEqualTo( dataElement ) )
            .findAny();

        if ( dataValue.isPresent() )
        {
            dataValue.get().setValue( value );
        }
        else
        {
            event.getDataValues()
                .add( createDataValue( bundle.getPreheat().getIdSchemes().toMetadataIdentifier( dataElement ),
                    value ) );
        }
    }

    /**
     * Tests whether the given values are equal. If the given value type is
     * numeric, the values are converted to doubles before being checked for
     * equality.
     *
     * @param value1 the first value.
     * @param value2 the second value.
     * @param valueType the value type.
     * @return true if the values are equal, false if not.
     */
    protected boolean isEqual( String value1, String value2, ValueType valueType )
    {
        if ( valueType.isNumeric() )
        {
            return NumberUtils.isParsable( value1 ) && NumberUtils.isParsable( value2 ) &&
                MathUtils.isEqual( Double.parseDouble( value1 ), Double.parseDouble( value2 ) );
        }
        else
        {
            return value1 != null && value1.equals( value2 );
        }
    }

    private DataValue createDataValue( MetadataIdentifier dataElement, String newValue )
    {
        return DataValue.builder()
            .dataElement( dataElement )
            .value( newValue )
            .build();
    }
}
