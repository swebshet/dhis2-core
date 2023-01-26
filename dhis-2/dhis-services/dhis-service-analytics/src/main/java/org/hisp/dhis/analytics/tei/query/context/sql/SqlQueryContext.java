/*
 * Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.analytics.tei.query.context.sql;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import org.apache.commons.lang3.tuple.Pair;
import org.hisp.dhis.analytics.common.query.Field;
import org.hisp.dhis.analytics.common.query.LeftJoin;
import org.hisp.dhis.analytics.common.query.Renderable;
import org.hisp.dhis.analytics.tei.TeiQueryParams;

@Builder( toBuilder = true )
@Data
public class SqlQueryContext
{
    public static final SqlQueryContext EMPTY_SQL_CONTEXT = SqlQueryContext.builder().build();

    @Singular
    private final List<Field> fields;

    @Singular
    private final List<LeftJoin> leftJoins;

    @Singular
    private final List<Pair<String, Renderable>> whereClauses;

    @Singular
    private final List<Renderable> orderClauses;

    private final TeiQueryParams teiQueryParams;

    private final SqlParameterManager sqlParameterManager;

}
