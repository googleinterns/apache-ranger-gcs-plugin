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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.hadoop.util.authorization.ActionType;
import com.google.cloud.hadoop.util.authorization.StorageRequestSummary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class RangerRequestTest {
    private static final String USER = "test-user";
    private static final String USERGROUPS = "grp1,grp2";

    private static final String READ = "read";
    private static final String WRITE = "write";

    private static final String BUCKET = "bucket";
    private static final String DIR = "/dir/path/";
    private static final String OBJ = DIR + "obj";
    private static final String ROOT = "/";

    @Test
    public void testListObject() throws IOException {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.LIST_OBJECT, Arrays.asList(peer));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 1).isTrue();
        verifyRequest(ret.get(0), READ, RangerRequestFactory.getObjectDirPath(peer));
    }

    @Test
    public void testInsertObject() throws IOException {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.INSERT_OBJECT, Arrays.asList(peer));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 1).isTrue();
        verifyRequest(ret.get(0), WRITE, RangerRequestFactory.getObjectDirPath(peer));
    }

    @Test
    public void testDeleteObject() throws IOException {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.DELETE_OBJECT, Arrays.asList(peer));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 1).isTrue();
        verifyRequest(ret.get(0), WRITE, RangerRequestFactory.getObjectDirPath(peer));
    }

    @Test
    public void testComposeObject() throws IOException {
        StorageRequestSummary.GcsStorage peer1 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "1", true);
        StorageRequestSummary.GcsStorage peer2 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "2", true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.COMPOSE_OBJECT, Arrays.asList(peer1, peer2));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 2).isTrue();
        verifyRequest(ret.get(0), WRITE, RangerRequestFactory.getObjectDirPath(peer1));
        verifyRequest(ret.get(1), READ, RangerRequestFactory.getObjectPath(peer2));
    }

    @Test
    public void testGetObject() throws IOException {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.GET_OBJECT, Arrays.asList(peer));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 1).isTrue();
        RangerRequest request = ret.get(0);
        verifyRequest(ret.get(0), READ, RangerRequestFactory.getObjectPath(peer));
    }

    @Test
    public void testRewriteObject() throws IOException {
        StorageRequestSummary.GcsStorage peer1 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "1", true);
        StorageRequestSummary.GcsStorage peer2 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "2", true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.REWRITE_OBJECT, Arrays.asList(peer1, peer2));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 2).isTrue();
        verifyRequest(ret.get(0), READ, RangerRequestFactory.getObjectPath(peer1));
        verifyRequest(ret.get(1), WRITE, RangerRequestFactory.getObjectDirPath(peer2));
    }

    @Test
    public void testCopyObject() throws IOException {
        StorageRequestSummary.GcsStorage peer1 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "1", true);
        StorageRequestSummary.GcsStorage peer2 = new StorageRequestSummary.GcsStorage(BUCKET, OBJ + "2", true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.COPY_OBJECT, Arrays.asList(peer1, peer2));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 2).isTrue();
        verifyRequest(ret.get(0), READ, RangerRequestFactory.getObjectPath(peer1));
        verifyRequest(ret.get(1), WRITE, RangerRequestFactory.getObjectDirPath(peer2));
    }

    @Test
    public void testPatchObject() throws IOException {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);
        StorageRequestSummary summary = new StorageRequestSummary(ActionType.PATCH_OBJECT, Arrays.asList(peer));

        List<RangerRequest> ret = RangerRequestFactory.createRangerRequests(summary, USER, USERGROUPS);

        assertThat(ret.size() == 1).isTrue();
        verifyRequest(ret.get(0), WRITE, RangerRequestFactory.getObjectPath(peer));
    }

    @Test
    public void testGetObjectPathEmptyObj() {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, null, true);

        String ret = RangerRequestFactory.getObjectPath(peer);

        assertThat(ret).isEqualTo(BUCKET + ROOT);
    }

    @Test
    public void testGetObjectPath() {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);

        String ret = RangerRequestFactory.getObjectPath(peer);

        assertThat(ret).isEqualTo(BUCKET + OBJ);
    }

    @Test
    public void testGetDirPathRoot() {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, ROOT + "obj", true);

        String ret = RangerRequestFactory.getObjectDirPath(peer);

        assertThat(ret).isEqualTo(BUCKET + ROOT);
    }

    @Test
    public void testGetDirPath() {
        StorageRequestSummary.GcsStorage peer = new StorageRequestSummary.GcsStorage(BUCKET, OBJ, true);

        String ret = RangerRequestFactory.getObjectDirPath(peer);

        assertThat(ret).isEqualTo(BUCKET + DIR);
    }

    private static void verifyRequest(RangerRequest request, String requiredPermission, String requiredResource) {
        assertThat(request.getUser()).isEqualTo(USER);
        assertThat(request.getUserGroups()).isEqualTo(USERGROUPS);
        assertThat(request.getResource()).isEqualTo(requiredResource);
        assertThat(request.getActions()).isEqualTo(requiredPermission);
    }
}
