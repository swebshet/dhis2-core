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
package org.hisp.dhis.proxy;

import static org.hisp.dhis.config.HibernateEncryptionConfig.AES_128_STRING_ENCRYPTOR;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.hisp.dhis.proxy.auth.ApiTokenAuth;
import org.hisp.dhis.proxy.auth.Auth;
import org.hisp.dhis.proxy.auth.HttpBasicAuth;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Morten Olav Hansen
 */
@Service
@RequiredArgsConstructor
public class ProxyService
{
    private final ProxyStore proxyStore;

    private final ObjectMapper objectMapper;

    @Qualifier( AES_128_STRING_ENCRYPTOR )
    private final PBEStringCleanablePasswordEncryptor encryptor;

    public Proxy getDecryptedById( String id )
    {
        Proxy proxy = proxyStore.getByUid( id );

        if ( proxy == null )
        {
            return null;
        }

        try
        {
            proxy = objectMapper.readValue( objectMapper.writeValueAsString( proxy ), Proxy.class );
            decrypt( proxy );
        }
        catch ( JsonProcessingException ignored )
        {
        }

        return proxy;
    }

    public ResponseEntity<String> getProxy( Proxy proxy, HttpServletRequest request )
    {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        proxy.getHeaders().forEach( headers::add );

        if ( proxy.getAuth() != null )
        {
            proxy.getAuth().apply( headers );
        }

        HttpHeaders queryParameters = new HttpHeaders();
        request.getParameterMap().forEach( ( key, value ) -> queryParameters.addAll( key, Arrays.asList( value ) ) );

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl( proxy.getUrl() )
            .queryParams( queryParameters );

        HttpEntity<String> entity = new HttpEntity<>( null, headers );

        return restTemplate.exchange( uriComponentsBuilder.toUriString(), HttpMethod.GET, entity, String.class,
            request.getParameterMap() );
    }

    private void decrypt( Proxy proxy )
    {
        Auth auth = proxy.getAuth();

        if ( auth == null )
        {
            return;
        }

        if ( auth.getType().equals( "api-token" ) )
        {
            ApiTokenAuth apiTokenAuth = (ApiTokenAuth) auth;
            apiTokenAuth.setToken( encryptor.decrypt( apiTokenAuth.getToken() ) );
        }
        else if ( auth.getType().equals( "http-basic" ) )
        {
            HttpBasicAuth httpBasicAuth = (HttpBasicAuth) auth;
            httpBasicAuth.setPassword( encryptor.decrypt( httpBasicAuth.getPassword() ) );
        }
    }
}
