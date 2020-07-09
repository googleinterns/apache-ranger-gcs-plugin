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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.apache.ranger.plugin.service.RangerBasePlugin;

import java.util.List;

/**
 * Accept RangerGcsRequests and return authorization result to users.
 * Handles Ranger auditing.
 */
public class RangerGcsPlugin {
    private static final Log LOG = LogFactory.getLog(RangerGcsPlugin.class);

    private RangerBasePlugin plugin;

    public RangerGcsPlugin() {
        plugin = new RangerBasePlugin("gcs", "gcs");
    }

    public void init() {
        plugin.init();
    }

    public void stop() {
        plugin.cleanup();
    }

    public RangerAccessResult isAccessAllowed(RangerGcsAccessRequest request, List<String> actions) {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsPlugin.isAccessAllowed(" +
                    request.getUser() + ", " + request.getAccessType() + ", " + request.getResource() +")");


        RangerGcsResource resource = (RangerGcsResource) request.getResource();
        RangerGcsAuditHandler handler = new RangerGcsAuditHandler(resource.getBucket(), resource.getObjectPath(), actions);

        RangerAccessResult result = null;
        for (String action: actions) {
            request.setAccessType(action);
            request.setAction(action);

            result = plugin.isAccessAllowed(request, handler);

            if (!result.getIsAccessDetermined() || !result.getIsAllowed()) {
                result.setIsAllowed(false);
                break;
            }
        }

        handler.flush();

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsPlugin.isAccessAllowed(" +
                    request.getUser() + ", " + request.getAccessType() + ", " + request.getResource() + "):" + result);

        return result;
    }
}
