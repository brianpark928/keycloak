/*
 * Copyright 2023 eStorm Inc
 */

package org.keycloak.admin.client.autootp;

import org.keycloak.representations.AccessTokenResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="mailto:aaa@bbb.ccc">xXx xxXXxx</a>
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public interface AutoOTPService {

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/autootp")
    AccessTokenResponse grantToken(@PathParam("realm") String realm, MultivaluedMap<String, String> map);

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/autootp")
    AccessTokenResponse refreshToken(@PathParam("realm") String realm, MultivaluedMap<String, String> map);

}
