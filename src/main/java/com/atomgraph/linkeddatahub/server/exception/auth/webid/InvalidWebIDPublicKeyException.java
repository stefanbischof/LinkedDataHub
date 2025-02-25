/**
 *  Copyright 2019 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.atomgraph.linkeddatahub.server.exception.auth.webid;

import java.security.interfaces.RSAPublicKey;
import javax.ws.rs.NotAuthorizedException;

/**
 * WebID authentication exception.
 * Thrown when the SSL client certificate public key does not match the public key of the WebID.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InvalidWebIDPublicKeyException extends NotAuthorizedException
{
    
    /** RSA public key */
    private final RSAPublicKey publicKey;
    /** WebID URI */
    private final String webID;
    
    /**
     * Constructs exception from public key and WebID URI.
     * 
     * @param publicKey RSA public key
     * @param webID WebID URI
     */
    public InvalidWebIDPublicKeyException(RSAPublicKey publicKey, String webID)
    {
        super("Client certificate public key did not match public key of WebID '" + webID + "'");
        this.publicKey = publicKey;
        this.webID = webID;
    }
    
    /**
     * Returns RSA public key.
     * 
     * @return public key
     */
    public RSAPublicKey getPublicKey()
    {
        return publicKey;
    }
    
    /**
     * Returns Web ID URI.
     * 
     * @return WebID URI
     */
    public String getWebID()
    {
        return webID;
    }
    
}
