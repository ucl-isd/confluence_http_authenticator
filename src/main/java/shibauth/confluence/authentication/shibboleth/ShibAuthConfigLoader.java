/*
 Copyright (c) 2008, Shibboleth Authenticator for Confluence Team
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of the Custom Space User Management Plugin Development Team
   nor the names of its contributors may be used to endorse or promote
   products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */

package shibauth.confluence.authentication.shibboleth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class ShibAuthConfigLoader {

    private final static Log log =
        LogFactory.getLog(ShibAuthConfigLoader.class);

    public static ShibAuthConfiguration getShibAuthConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing authenticator using property file "
                      + ShibAuthConstants.PROPERTIES_FILE);
        }

        InputStream propsIn =
            RemoteUserAuthenticator.class.getResourceAsStream(ShibAuthConstants.PROPERTIES_FILE);

        ShibAuthConfiguration config = new ShibAuthConfiguration();

        try {
            Properties configProps = new Properties();
            configProps.load(propsIn);

            // Load create.users property
            config.setCreateUsers( Boolean.valueOf(
                configProps.getProperty(ShibAuthConstants.CREATE_USERS)).booleanValue());

            if (log.isDebugEnabled()) {
                log.debug("Setting create new users to " + config.isCreateUsers());
            }

            // Load update.info property
            config.setUpdateInfo( Boolean.valueOf(
                configProps.getProperty(ShibAuthConstants.UPDATE_INFO)).booleanValue());

            if (log.isDebugEnabled()) {
                log.debug("Setting update user information to " + config.isUpdateInfo());
            }

            // Load update.roles property
            config.setUpdateRoles( Boolean.valueOf(
                configProps.getProperty(ShibAuthConstants.UPDATE_ROLES)).booleanValue());

            if (log.isDebugEnabled()) {
                log.debug("Setting update user roles to " + config.isUpdateRoles());
            }

            // Load convert.to.utf8 property
            config.setUpdateRoles( Boolean.valueOf(
                configProps.getProperty(ShibAuthConstants.CONVERT_TO_UTF8)).booleanValue());

            if (log.isDebugEnabled()) {
                log.debug("Setting convert header values to UTF-8 to " + config.isConvertToUTF8());
            }


            // Load default roles
            List defaultRoles = new ArrayList();

            String roles = configProps.getProperty(ShibAuthConstants.DEFAULT_ROLES);

            if (roles != null) {

                defaultRoles.addAll(StringUtil.toListOfNonEmptyStringsDelimitedByCommaOrSemicolon(roles));

                if (log.isDebugEnabled()) {
                    for (Iterator it =
                            defaultRoles.iterator(); it.hasNext(); ) {
                        log.debug("Adding role " + it.next().toString()
                                  + " to list of default user roles");
                    }
                }
            }

            config.setDefaultRoles(defaultRoles);

            config.setFullNameHeaderName(configProps.getProperty(ShibAuthConstants.FULLNAME_HEADER_NAME_PROPERTY));

            if (log.isDebugEnabled()) {
                log.debug(
                    "HTTP Header that may contain user's full name set to: "
                    + config.getFullNameHeaderName());
            }

            config.setEmailHeaderName(configProps.getProperty(ShibAuthConstants.EMAIL_HEADER_NAME_PROPERTY));

            if (log.isDebugEnabled()) {
                log.debug(
                    "HTTP Header that may contain user's email address set to: "
                    + config.getEmailHeaderName());
            }

            // fill in the header names to be monitored
            Set attribHeaders = new HashSet();

            // Load dynamic roles property
            String attribNameStr =
                configProps.getProperty(ShibAuthConstants.ROLES_ATTRIB_NAMES);

            if (attribNameStr != null) {
                List attribNames = StringUtil.toListOfNonEmptyStringsDelimitedByCommaOrSemicolon(attribNameStr);

                if (log.isDebugEnabled()) {
                    for (Iterator it = attribNames.iterator(); it.hasNext(); ) {

                        // According to Bruc Liong, this is case-insensitive to make it easier on the admin.

                        String lowercaseAttrib =
                            it.next().toString().trim().toLowerCase();

                        log.debug("Reading dynamic attribute: " + lowercaseAttrib);
                        attribHeaders.add(lowercaseAttrib);
                    }
                }
            }

            config.setAttribHeaders(attribHeaders);

            Map mapRole = new HashMap();
            // remember the map from incoming attribute to confluence's group
            for (Enumeration propEnum = configProps.propertyNames();
                    propEnum.hasMoreElements(); ) {
                String prop = propEnum.nextElement().toString();

                // register as lower case in the map (dont think there would be
                // conflict between upper/lower cases
                String shibAttribFromConfig = prop.trim().toLowerCase();

                if (shibAttribFromConfig.startsWith(ShibAuthConstants.ROLES_ATTRIB_PREFIX)
                        &&!shibAttribFromConfig.startsWith(
                            ShibAuthConstants.ROLES_ATTRIB_NAMES)) {
                    String roleStr = configProps.getProperty(prop);
                    String roleKey = shibAttribFromConfig.substring(ShibAuthConstants.ROLES_ATTRIB_PREFIX.length());

                    //this is the map from shib_key = conf_group1, group2, etc
                    mapRole.put(roleKey,
                                StringUtil.toListOfNonEmptyStringsDelimitedByCommaOrSemicolon(roleStr));

                    if (log.isDebugEnabled()) {
                        log.debug("Found role mapping declared as " + prop);
                    }
                }
            }

            config.setMapRole(mapRole);

        } catch (IOException e) {
            log.warn(
                "Unable to read properties file, using default properties", e);
        }

        return config;
    }
}