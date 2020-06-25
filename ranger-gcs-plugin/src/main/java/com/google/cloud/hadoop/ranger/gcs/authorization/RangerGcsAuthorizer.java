/*
 *
 *  * Copyright 2020 Google Inc. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.google.cloud.hadoop.ranger.gcs.authorization;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;

import java.util.List;
import java.util.Set;

/**
 * Accept user, user groups, resource and actions information and return authorization result to users.
 */
public class RangerGcsAuthorizer {
    private static final Log LOG = LogFactory.getLog(RangerGcsAuthorizer.class);

    private RangerGcsPlugin plugin;

    public RangerGcsAuthorizer() {
        plugin = new RangerGcsPlugin();
    }

    public void init() {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsAuthorizer.init()");

        plugin.init();

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsAuthorizer.init()");
    }

    public void stop() {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsAuthorizer.stop()");

        if (plugin != null) {
            RangerGcsPlugin tmp_plugin = plugin;
            plugin = null;
            tmp_plugin.stop();
        }

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsAuthorizer.stop()");
    }

    public RangerGcsPermissionCheckResult isAccessAllowed(String user, Set<String> userGroups,
                                                          String bucket, String objectPath,
                                                          List<String> actions) {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsAuthorizer(" + user +", " +
                    bucket + ", " + objectPath + ", " + String.join("/", actions) + ")");

        RangerGcsResource resource = new RangerGcsResource(bucket, objectPath);
        RangerGcsAccessRequest request = new RangerGcsAccessRequest(resource, user, userGroups);

        RangerGcsPermissionCheckResult ret = null;
        RangerAccessResult result = null;
        try {
            result = plugin.isAccessAllowed(request, actions);
        } catch (Exception ex) {
            ret = RangerGcsPermissionCheckResult.Deny();
            result = null;
            if (LOG.isErrorEnabled())
                LOG.error("Error happen during plugin.isAccessAllowed().", ex);
        }

        if (result == null || !result.getIsAccessDetermined() || !result.getIsAllowed()) {
            ret = RangerGcsPermissionCheckResult.Deny();
        } else {
            ret = RangerGcsPermissionCheckResult.Allow();
        }

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsAuthorizer(" + user +", " +
                    bucket + ", " + objectPath + ", " + String.join("/", actions) + "): " + ret.getMessage());

        return ret;
    }
}
