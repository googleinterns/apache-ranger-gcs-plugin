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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestTicket {
    private String user = null;
    private Set<String> userGroups = new HashSet<>();
    private String bucket = null;
    private String objectPath = null;
    private List<String> actions = new ArrayList<>();
    private RangerGcsPermissionCheckResult result = null;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Ticket(user=").append(getUser())
                .append(", userGroup=").append(String.join(",", getUserGroups()))
                .append(", bucket=").append(getBucket())
                .append(", objectPath=").append(getObjectPath())
                .append(", actions=").append(String.join(",", getActions()))
                .append(")")
                .toString();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Set<String> getUserGroups() {
        return userGroups;
    }

    public void addUserGroup(String group) {
        this.userGroups.add(group);
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public RangerGcsPermissionCheckResult getResult() {
        return result;
    }

    public void setResult(RangerGcsPermissionCheckResult result) {
        this.result = result;
    }
}
