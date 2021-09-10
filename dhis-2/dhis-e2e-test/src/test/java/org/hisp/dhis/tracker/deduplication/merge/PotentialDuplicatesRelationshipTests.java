/*
 * Copyright (c) 2004-2021, University of Oslo
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

package org.hisp.dhis.tracker.deduplication.merge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.hisp.dhis.Constants;
import org.hisp.dhis.actions.LoginActions;
import org.hisp.dhis.actions.UserActions;
import org.hisp.dhis.actions.UserRoleActions;
import org.hisp.dhis.actions.tracker.RelationshipActions;
import org.hisp.dhis.dto.TrackerApiResponse;
import org.hisp.dhis.helpers.JsonObjectBuilder;
import org.hisp.dhis.tracker.deduplication.PotentialDuplicatesApiTest;
import org.hisp.dhis.utils.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PotentialDuplicatesRelationshipTests
    extends PotentialDuplicatesApiTest
{
    @BeforeEach
    public void beforeEach()
    {
        loginActions.loginAsSuperUser();
    }

    @Test
    public void shouldManuallyMergeRelationships()
    {
        // arrange
        String teiA = createTei();
        String teiB = createTei();
        String teiC = createTei();

        String relationship = createUniDirectionalRelationship( teiB, teiC ).extractImportedRelationships().get( 0 );
        String relationship2 = createUniDirectionalRelationship( teiA, teiC ).extractImportedRelationships().get( 0 );
        String relationship3 = createRelationship( teiA, teiB ).extractImportedRelationships().get( 0 );

        JsonObject mergeObject = new JsonObjectBuilder()
            .addArray( "relationships", Arrays.asList( relationship, relationship2, relationship3 ) ).build();

        String potentialDuplicate = potentialDuplicatesActions.createAndValidatePotentialDuplicate( teiA, teiB, "OPEN" );

        String username = createUserWithAccessToMerge();
        new LoginActions().loginAsUser( username, Constants.USER_PASSWORD );

        potentialDuplicatesActions.autoMergePotentialDuplicate( potentialDuplicate )
            .validate().statusCode( 200 );

        trackerActions.get( "/trackedEntities/" + teiA + "?fields=*" )
            .validate().statusCode( 200 )
            .body( "relationships", hasSize( 2 ) )
            .body( "relationships[0].to.trackedEntity", equalTo( teiC ) );

        trackerActions.get( "/trackedEntities/" + teiC + "?fields=*" )
            .validate().statusCode( 200 )
            .body( "relationships", hasSize( 1 ) )
            .body( "relationships[0].from.trackedEntity", equalTo( teiA ) );
    }

    @Test
    public void shouldManuallyMergeRelationship()
    {
        String teiA = createTei();
        String teiB = createTei();
        String teiC = createTei();

        String relationship = createRelationship( teiB, teiC ).extractImportedRelationships().get( 0 );

        String potentialDuplicate = potentialDuplicatesActions.createAndValidatePotentialDuplicate( teiA, teiB, "OPEN" );

        potentialDuplicatesActions.manualMergePotentialDuplicate( potentialDuplicate,
            new JsonObjectBuilder().addArray( "relationships", Arrays.asList( relationship ) ).build() )
            .validate().statusCode( 200 );

        trackerActions.getTrackedEntity( teiA )
            .validate()
            .statusCode( 200 )
            .body( "relationships", hasSize( 1 ) )
            .body( "relationships.relationship", hasItems( relationship ) );
    }

    @Test
    public void shouldRemoveDuplicateRelationship()
    {
        // arrange
        String teiA = createTei();
        String teiB = createTei();

        String relationship = createRelationship( teiA, teiB ).extractImportedRelationships().get( 0 );

        JsonArray relationships = new JsonArray();
        relationships.add( relationship );

        String potentialDuplicate = potentialDuplicatesActions.createAndValidatePotentialDuplicate( teiA, teiB, "OPEN" );

        potentialDuplicatesActions.autoMergePotentialDuplicate( potentialDuplicate ).validate().statusCode( 200 );

        trackerActions.get( "/trackedEntities/" + teiA + "?fields=*" )
            .validate().statusCode( 200 )
            .body( "relationships", hasSize( 0 ) );

        new RelationshipActions().get( relationship ).validateStatus( 404 );
    }

    private TrackerApiResponse createRelationship( String teiA, String teiB )
    {
        JsonObject payload = JsonObjectBuilder
            .jsonObject( trackerActions.buildBidirectionalTrackedEntityRelationship( teiA, teiB ) )
            .wrapIntoArray( "relationships" );

        return trackerActions.postAndGetJobReport( payload )
            .validateSuccessfulImport();
    }

    private TrackerApiResponse createUniDirectionalRelationship( String teiA, String teiB )
    {
        JsonObject payload = JsonObjectBuilder
            .jsonObject( trackerActions.buildNonBidirectionalTrackedEntityRelationship( teiA, teiB ) )
            .wrapIntoArray( "relationships" );

        return trackerActions.postAndGetJobReport( payload )
            .validateSuccessfulImport();
    }
}
