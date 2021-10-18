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

package com.google.cloud.hadoop.ranger.gcs.connectorAdapter;

/**
 * Request to be sent to Ranger.
 * Includes necessary information for Ranger to perform authorization.
 */
public class RangerRequest {
    private final String user;
    private final String userGroups;
    private final String resource;
    private final String actions;

    public RangerRequest(String user, String userGroups, String resource, String actions) {
        this.user = user;
        this.userGroups = userGroups;
        this.resource = resource;
        this.actions = actions;
    }

    public String getUser() {
        return user;
    }

    public String getUserGroups() {
        return userGroups;
    }

    public String getResource() {
        return resource;
    }

    public String getActions() {
        return actions;
    }
}
