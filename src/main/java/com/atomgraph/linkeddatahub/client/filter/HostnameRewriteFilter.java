/**
 *  Copyright 2021 Martynas Jusevičius <martynas@atomgraph.com>
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
package com.atomgraph.linkeddatahub.client.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author {@literal Martynas Jusevičius <martynas@atomgraph.com>}
 */
public class HostnameRewriteFilter implements ClientRequestFilter
{

    private static final Logger log = LoggerFactory.getLogger(HostnameRewriteFilter.class);

    private final URI baseURI;
    private final String hostname;
    private final Map<Integer, Integer> portMapping;

    public HostnameRewriteFilter(URI baseURI, String hostname, Map<Integer, Integer> portMapping)
    {
        this.baseURI = baseURI;
        this.hostname = hostname;
        this.portMapping = portMapping;
    }
    
    @Override
    public void filter(ClientRequestContext cr) throws IOException
    {
        if (getBaseURI().relativize(cr.getUri()).isAbsolute()) return; // don't rewrite URIs that are not relative to the base URI (e.g. SPARQL Protocol URLs)

        try
        {
            Integer port = cr.getUri().getPort();
            // map default ports which are not explicit in the request URI
            if (port == -1 && cr.getUri().getScheme().equals("http")) port = 80;
            if (port == -1 && cr.getUri().getScheme().equals("https")) port = 443;
            
            if (getPortMapping().containsKey(port)) port = getPortMapping().get(port); // map port numbers
                
            URI newUri = new URI(cr.getUri().getScheme(), cr.getUri().getUserInfo(), getHostname(), port,
                    cr.getUri().getPath(), cr.getUri().getQuery(), cr.getUri().getFragment());
            
        
            if (log.isDebugEnabled()) log.debug("Rewriting client request URI from '{}' to '{}'", cr.getUri(), newUri);
            cr.setUri(newUri);
        }
        catch (URISyntaxException ex)
        {
            // shouldn't happen
        }
    }
    
    public URI getBaseURI()
    {
        return baseURI;
    }
    
    public String getHostname()
    {
        return hostname;
    }

    public Map<Integer, Integer> getPortMapping()
    {
        return portMapping;
    }

}
