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
package org.hisp.dhis.analytics.tei;

import static org.springframework.util.Assert.notNull;

import lombok.RequiredArgsConstructor;

import org.hisp.dhis.analytics.common.CommonQueryRequest;
import org.hisp.dhis.analytics.shared.GridAdaptor;
import org.hisp.dhis.analytics.shared.QueryExecutor;
import org.hisp.dhis.analytics.shared.SqlQuery;
import org.hisp.dhis.analytics.shared.SqlQueryResult;
import org.hisp.dhis.analytics.tei.query.QueryContext;
import org.hisp.dhis.analytics.tei.query.TeiFullQuery;
import org.hisp.dhis.common.Grid;
import org.springframework.stereotype.Service;

/**
 * Service responsible exclusively for querying. Methods present on this class
 * must not change any state.
 *
 * @author maikel arabori
 */
@Service
@RequiredArgsConstructor
public class TeiAnalyticsQueryService
{
    private final QueryExecutor<SqlQuery, SqlQueryResult> queryExecutor;

    private final GridAdaptor gridAdaptor;

    /**
     * This method will create a query, based on the teiParams, and execute it
     * against the underline data provider and return. The results found will be
     * returned encapsulated on a Grid object.
     *
     * @param teiQueryParams
     * @return the populated Grid object
     * @throws IllegalArgumentException if the given teiParams is null
     */
    public Grid getGrid( final TeiQueryParams teiQueryParams, final CommonQueryRequest commonQueryRequest )
    {
        QueryContext queryContext = QueryContext.of( teiQueryParams );
        // output should look like: https://pastebin.com/4aK9ZxEQ

        notNull( teiQueryParams, "The 'teiQueryParams' must not be null" );
        notNull( commonQueryRequest, "The 'commonQueryRequest' must not be null" );

        final SqlQueryResult result = queryExecutor.execute( new TeiFullQuery( queryContext ).statement() );

        return gridAdaptor.createGrid( result, teiQueryParams, commonQueryRequest );
    }
}
