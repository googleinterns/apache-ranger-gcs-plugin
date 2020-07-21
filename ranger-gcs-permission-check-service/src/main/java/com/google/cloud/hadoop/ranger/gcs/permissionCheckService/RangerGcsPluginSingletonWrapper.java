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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService;

import com.google.cloud.hadoop.ranger.gcs.authorization.RangerGcsAuthorizer;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RangerGcsPluginWrapper;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

/**
 * Maintain a singleton Ranger plugin instance.
 * Ranger plugin sets it's policy engine during policy synchronization with Ranger server.
 * Although the plugin also sets it's policy engine during initialization, the process creates an extra
 *  policy refresh daemon thread.
 *
 * The design here uses a singleton instance to keep a already-initialized instance without creating new
 *  daemon threads. There might be other ways to solve this issue, but this is good enough for now.
 */
public class RangerGcsPluginSingletonWrapper implements RangerGcsPluginWrapper {
    private static Log LOG = LogFactory.getLog(RangerGcsPluginSingletonWrapper.class);
    private static RangerGcsPluginSingletonWrapper instance;

    private RangerGcsAuthorizer authorizer = null;

    private RangerGcsPluginSingletonWrapper () {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsPluginSingletonWrapper()");

        authorizer = new RangerGcsAuthorizer();

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsPluginSingletonWrapper()");
    };

    public static RangerGcsPluginSingletonWrapper getInstance() {
        if (instance == null) {
            synchronized (RangerGcsPluginSingletonWrapper.class) {
                if (instance == null)
                    instance = new RangerGcsPluginSingletonWrapper();
            }
        }

        return instance;
    }

    public void init() {
        if (authorizer != null)
            authorizer.init();
    }

    public void stop() {
        if (authorizer != null)
            authorizer.stop();
    }

    public RangerGcsPermissionCheckResult isAccessAllowed(String user, Set<String> userGroups,
                                                          String bucket, String objectPath,
                                                          List<String> actions) {

        if (authorizer != null) {
            return authorizer.isAccessAllowed(user, userGroups, bucket, objectPath, actions);
        }
        return RangerGcsPermissionCheckResult.Deny();
    }
}
