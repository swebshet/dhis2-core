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
package org.hisp.dhis.security.oidc;

import java.util.Arrays;
import java.util.function.Function;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.jwk.JWK;

@Service
public class JwtPrivateAuthorizationCodeTokenResponseClient
    implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
{
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new OAuth2AuthorizationCodeGrantRequestEntityConverter();

    private RestOperations restOperations;

    public JwtPrivateAuthorizationCodeTokenResponseClient()
    {

        Function<ClientRegistration, JWK> jwkResolver = ( clientRegistration ) -> {
            if ( clientRegistration.getClientAuthenticationMethod()
                .equals( ClientAuthenticationMethod.PRIVATE_KEY_JWT ) )
            {
                // Assuming RSA key type
                //                RSAPublicKey publicKey = ...
                //                RSAPrivateKey privateKey = ...
                //                return new RSAKey.Builder(publicKey)
                //                        .privateKey(privateKey)
                //                        .keyID(UUID.randomUUID().toString())
                //                        .build();
                return null;

            }
            return null;
        };

        NimbusJwtClientAuthenticationParametersConverter con = new NimbusJwtClientAuthenticationParametersConverter(
            jwkResolver );

        setRequestEntityConverter( con );

        RestTemplate restTemplate = new RestTemplate(
            Arrays.asList( new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter() ) );
        restTemplate.setErrorHandler( new OAuth2ErrorResponseErrorHandler() );
        this.restOperations = restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(
        OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest )
    {
        Assert.notNull( authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null" );
        RequestEntity<?> request = this.requestEntityConverter.convert( authorizationCodeGrantRequest );
        ResponseEntity<OAuth2AccessTokenResponse> response = getResponse( request );
        // As per spec, in Section 5.1 Successful Access Token Response
        // https://tools.ietf.org/html/rfc6749#section-5.1
        // If AccessTokenResponse.scope is empty, then we assume all requested scopes were
        // granted.
        // However, we use the explicit scopes returned in the response (if any).

        return response.getBody();
    }

    private ResponseEntity<OAuth2AccessTokenResponse> getResponse( RequestEntity<?> request )
    {
        try
        {
            return this.restOperations.exchange( request, OAuth2AccessTokenResponse.class );
        }
        catch ( RestClientException ex )
        {
            OAuth2Error oauth2Error = new OAuth2Error( INVALID_TOKEN_RESPONSE_ERROR_CODE,
                "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
                    + ex.getMessage(),
                null );
            throw new OAuth2AuthorizationException( oauth2Error, ex );
        }
    }

    /**
     * Sets the {@link Converter} used for converting the
     * {@link OAuth2AuthorizationCodeGrantRequest} to a {@link RequestEntity}
     * representation of the OAuth 2.0 Access Token Request.
     *
     * @param requestEntityConverter the {@link Converter} used for converting
     *        to a {@link RequestEntity} representation of the Access Token
     *        Request
     */
    public void setRequestEntityConverter(
        Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter )
    {
        Assert.notNull( requestEntityConverter, "requestEntityConverter cannot be null" );
        this.requestEntityConverter = requestEntityConverter;
    }

    /**
     * Sets the {@link RestOperations} used when requesting the OAuth 2.0 Access
     * Token Response.
     *
     * <p>
     * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be
     * configured with the following:
     * <ol>
     * <li>{@link HttpMessageConverter}'s - {@link FormHttpMessageConverter} and
     * {@link OAuth2AccessTokenResponseHttpMessageConverter}</li>
     * <li>{@link ResponseErrorHandler} -
     * {@link OAuth2ErrorResponseErrorHandler}</li>
     * </ol>
     *
     * @param restOperations the {@link RestOperations} used when requesting the
     *        Access Token Response
     */
    public void setRestOperations( RestOperations restOperations )
    {
        Assert.notNull( restOperations, "restOperations cannot be null" );
        this.restOperations = restOperations;
    }
}
