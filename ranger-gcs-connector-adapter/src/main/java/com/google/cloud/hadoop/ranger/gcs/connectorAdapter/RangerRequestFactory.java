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

import java.net.URI;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create RangerRequest from StorageRequestSummary.
 */
public class RangerRequestFactory {

    public static final String READ = "read";
    public static final String WRITE = "write";

    private String user;
    private String userGroups;

    public RangerRequestFactory() throws IOException {
        UserGroupInformation userInfo = UserGroupInformation.getCurrentUser();
        this.user = userInfo.getUserName();
        this.userGroups = String.join(",", userInfo.getGroups());
    }

    RangerRequestFactory(String user, String userGroups) {
        this.user = user;
        this.userGroups = userGroups;
    }

    /**
     * Create a RangerRequest that requires read permission on object's dir.
     * @param resource A prefix URI of GCS object.
     * @return List of RangerRequest.
     */
    public RangerRequest createReadRequestOnDir(URI resource) {
       return new RangerRequest(user, userGroups, getObjectDirPath(resource), READ);
    }

    /**
     * Create a RangerRequest that requires write permission on object's dir.
     * @param resource URI of GCS object.
     * @return List of RangerRequest.
     */
    public RangerRequest createWriteRequestOnDir(URI resource) {
        return new RangerRequest(user, userGroups, getObjectDirPath(resource), WRITE);
    }

    /**
     * Create a list of required RangerRequests for compose object request.
     * @param destination URI of destination object.
     * @param sources List of URI of source objects.
     * @return List of RangerRequest.
     */
    public List<RangerRequest> createComposeObjectRequests(URI destination, List<URI> sources) {
        List<RangerRequest> ret = new ArrayList<>();
        // User should have read permission on source objects
        ret.add(new RangerRequest(user, userGroups, getObjectDirPath(destination), WRITE));
        // Need to have read permission on all the source objects
        for (URI peer: sources) {
            ret.add(new RangerRequest(user, userGroups, getObjectPath(peer), READ));
        }
        return ret;
    }

    /**
     * Create a RangerRequest that requires read permission on the object.
     * @param resource URI of GCS object.
     * @return List of RangerRequest
     */
    public RangerRequest createReadRequestOnObject(URI resource) {
        return new RangerRequest(user, userGroups, getObjectPath(resource), READ);
    }

    /**
     * Create RangerRequests that requires read permission on the source object, and write
     * permission on the destination object's path.
     * @param source
     * @param destination
     * @return
     */
    public List<RangerRequest> createSourceToDestinationRequests(URI source, URI destination) {
        List<RangerRequest> ret = new ArrayList<>();
        ret.add(new RangerRequest(user, userGroups, getObjectPath(source), READ));
        ret.add(new RangerRequest(user, userGroups, getObjectDirPath(destination), WRITE));
        return ret;
    }

    /**
     * Create a RangerRequest that requires write permission on the object.
     * @param resource A URI of GCS object.
     * @return A RangerRequest.
     */
    public RangerRequest createWriteRequestOnObject(URI resource) {
        return new RangerRequest(user, userGroups, getObjectPath(resource), WRITE);
    }

    /**
     * Get the object path from bucket/path string.
     */
    protected static String getObjectPath(URI resource) {
        String objectPath = resource.getPath().isEmpty() ? "/" : resource.getPath();
        return String.format("%s%s", resource.getAuthority(),
                objectPath.charAt(0) == '/' ? objectPath : "/" + objectPath);
    }

    /**
     * Get the directory of the object from bucket/path string.
     * In other word, remove the base file name from path string.
     */
    protected static String getObjectDirPath(URI resource) {
        String ret = getObjectPath(resource);
        return ret.substring(0, ret.lastIndexOf('/') + 1);
    }
}
