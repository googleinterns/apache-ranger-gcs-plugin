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

import com.google.cloud.hadoop.util.authorization.StorageRequestSummary;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Create RangerRequest from StorageRequestSummary.
 */
public class RangerRequestFactory {
    public static final RangerRequest DENY = new RangerRequest.DenyRequest();

    private static final String READ = "read";
    private static final String WRITE = "write";

    public static List<RangerRequest> createRangerRequests(StorageRequestSummary storageRequestSummary) throws IOException {
        UserGroupInformation userInfo = UserGroupInformation.getCurrentUser();
        String user = userInfo.getUserName();
        String userGroups = String.join(",", userInfo.getGroups());
        return createRangerRequests(storageRequestSummary, user, userGroups);
    }

    public static List<RangerRequest> createRangerRequests(StorageRequestSummary storageRequestSummary,
                                                          String user, String userGroups) throws IOException {
        List<RangerRequest> ret = new ArrayList<>();

        switch (storageRequestSummary.getActionType()) {
             // List an object of a prefix.
            case LIST_OBJECT:
                // User should have read permission on the directory.
                ret.add(new RangerRequest(user, userGroups,
                        getObjectDirPath(storageRequestSummary.getResources().get(0)),
                        READ));
                break;

            // Insert an object in a directory.
            case INSERT_OBJECT:

            // Delete an object.
            case DELETE_OBJECT:
                // User should have write permission on the directory.
                ret.add(new RangerRequest(user, userGroups,
                        getObjectDirPath(storageRequestSummary.getResources().get(0)),
                        WRITE));
                break;

            // Compose different objects into one.
            // User should have read permission on source objects
            //  and write permission to the destination object's directory.
            case COMPOSE_OBJECT:
                // The first element is the destination object.
                ret.add(new RangerRequest(user, userGroups,
                        getObjectDirPath(storageRequestSummary.getResources().get(0)),
                        WRITE));
                // Need to have read permission on all the source objects
                for (StorageRequestSummary.GcsStorage peer:
                        storageRequestSummary.getResources().subList(1, storageRequestSummary.getResources().size())) {
                    ret.add(new RangerRequest(user, userGroups,
                            getObjectPath(peer),
                            READ));
                }
                break;

            // Get an object.
            // User should have read permission on the object.
            case GET_OBJECT:
                ret.add(new RangerRequest(user, userGroups,
                        getObjectPath(storageRequestSummary.getResources().get(0)),
                        READ));
                break;

            // Rewrite an object.
            case REWRITE_OBJECT:

            // Copy Object.
            case COPY_OBJECT:
                // User should have read permission source object
                //  and  write permission on the destination's directory.
                ret.add(new RangerRequest(user, userGroups,
                        getObjectPath(storageRequestSummary.getResources().get(0)),
                        READ));
                ret.add(new RangerRequest(user, userGroups,
                        getObjectDirPath(storageRequestSummary.getResources().get(1)),
                        WRITE));
                break;

            // Patch object.
            case PATCH_OBJECT:
                // User should have write permission on the object.
                ret.add(new RangerRequest(user, userGroups,
                        getObjectPath(storageRequestSummary.getResources().get(0)),
                        WRITE));
                break;

            // List bucket.
            case LIST_BUCKET:

            // Insert bucket.
            case INSERT_BUCKET:

            // Get bucket.
            case GET_BUCKET:

            // Delete bucket
            case DELETE_BUCKET:
                // User should have read/write or both permission on a project.
                // Out of scope.
                break;

            // Unknown request type. Deny it just to be safe.
            default:
                ret.add(DENY);
        }

        return ret;
    }

    /**
     * Get the object path from bucket/path string.
     */
    protected static String getObjectPath(StorageRequestSummary.GcsStorage peer) {
        String objectPath = peer.getObject() == null || peer.getObject().isEmpty() ? "/" : peer.getObject();
        return String.format("%s/%s", peer.getBucket(),
                objectPath.charAt(0) == '/' ? objectPath.substring(1) : objectPath);
    }

    /**
     * Get the directory of the object from bucket/path string.
     * In other word, remove the base file name from path string.
     */
    protected static String getObjectDirPath(StorageRequestSummary.GcsStorage peer) {
        String ret = getObjectPath(peer);
        return ret.substring(0, ret.lastIndexOf('/') + 1);
    }
}
