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
package org.hisp.dhis.dxf2.events.importer.insert.preprocess;

import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.importer.Processor;
import org.hisp.dhis.dxf2.events.importer.context.WorkContext;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.springframework.stereotype.Component;

/**
 * This PreProcessor tries to assign a ProgramStage to an event
 *
 * @author Luciano Fiandesio
 */
@Component
public class ProgramStagePreProcessor implements Processor
{
    @Override
    public void process( Event event, WorkContext ctx )
    {
        Program program = ctx.getProgramsMap().get( event.getProgram() );
        if ( program == null )
        {
            return; // Program is a mandatory value, it will be caught by the
                   // validation
        }
        ProgramStage programStage = ctx.getProgramStage(
            ctx.getImportOptions().getIdSchemes().getProgramStageIdScheme(), event.getProgramStage() );

        if ( programStage == null && program.isWithoutRegistration() )
        {
            programStage = program.getProgramStageByStage( 1 );

        }
        if ( programStage != null )
        {
            event.setProgramStage(
                IdentifiableObjectUtils.getIdentifierBasedOnIdScheme( programStage,
                    ctx.getImportOptions().getIdSchemes().getProgramStageIdScheme() ) );
        }
    }
}
