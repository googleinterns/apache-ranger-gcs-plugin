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

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.List;

@RunWith(JUnit4.class)
public class RangerRequestTest {
    private static final String READ = "read";
    private static final String WRITE = "write";

    private static final String USER = "test-user";
    private static final String USER_GROUPS = "test-groups";

    private static final String BUCKET = "bucket";
    private static final String DIR = "/dir/path/";
    private static final String OBJ = DIR + "obj";
    private static final String ROOT = "/";

    private static final URI TEST_URI = URI.create("gs://" + BUCKET + OBJ);

    private RangerRequestFactory factory;

    @Before
    public void init() {
        this.factory = new RangerRequestFactory(USER, USER_GROUPS);
    }

    @Test
    public void testCreateReadRequestOnDir() throws IOException {
        RangerRequest ret = factory.createReadRequestOnDir(TEST_URI);

        verifyRequest(ret, READ, RangerRequestFactory.getObjectDirPath(TEST_URI));
    }

    @Test
    public void testCreateWriteRequestOnDir() throws IOException {
        RangerRequest ret = factory.createWriteRequestOnDir(TEST_URI);

        verifyRequest(ret, WRITE, RangerRequestFactory.getObjectDirPath(TEST_URI));
    }

    @Test
    public void testCreateReadRequestOnObject() throws IOException {
        RangerRequest ret = factory.createReadRequestOnObject(TEST_URI);

        verifyRequest(ret, READ, RangerRequestFactory.getObjectPath(TEST_URI));
    }

    @Test
    public void testCreateWriteRequestOnObject() throws IOException {
        RangerRequest ret = factory.createWriteRequestOnObject(TEST_URI);

        verifyRequest(ret, WRITE, RangerRequestFactory.getObjectPath(TEST_URI));
    }

    @Test
    public void testComposeObject() throws IOException {
        URI destination = URI.create("gs://" + BUCKET + OBJ);
        URI source1 = URI.create("gs://" + BUCKET + OBJ + "1");
        URI source2 = URI.create("gs://" + BUCKET + OBJ + "2");

        List<RangerRequest> ret = factory.createComposeObjectRequests(destination,
            ImmutableList.of(
                source1,
                source2
            ));

        assertThat(ret.size() == 3).isTrue();
        verifyRequest(ret.get(0), WRITE, RangerRequestFactory.getObjectDirPath(destination));
        verifyRequest(ret.get(1), READ, RangerRequestFactory.getObjectPath(source1));
        verifyRequest(ret.get(2), READ, RangerRequestFactory.getObjectPath(source2));
    }

    @Test
    public void testSourceToDestination() throws IOException {
        URI source = URI.create("gs://" + BUCKET + OBJ + "1");
        URI destination = URI.create("gs://" + BUCKET + OBJ + "2");

        List<RangerRequest> ret = factory.createSourceToDestinationRequests(source, destination);

        assertThat(ret.size() == 2).isTrue();
        verifyRequest(ret.get(0), READ, RangerRequestFactory.getObjectPath(source));
        verifyRequest(ret.get(1), WRITE, RangerRequestFactory.getObjectDirPath(destination));
    }

    @Test
    public void testGetObjectPathEmptyObj() {
        URI resource = URI.create("gs://" + BUCKET);

        String ret = RangerRequestFactory.getObjectPath(resource);

        assertThat(ret).isEqualTo(BUCKET + ROOT);
    }

    @Test
    public void testGetObjectPath() {
        URI resource = URI.create("gs://" + BUCKET + OBJ);

        String ret = RangerRequestFactory.getObjectPath(resource);

        assertThat(ret).isEqualTo(BUCKET + OBJ);
    }

    @Test
    public void testGetDirPathRoot() {
        URI resource = URI.create("gs://" + BUCKET + ROOT + "test-obj");

        String ret = RangerRequestFactory.getObjectDirPath(resource);

        assertThat(ret).isEqualTo(BUCKET + ROOT);
    }

    @Test
    public void testGetDirPath() {
        URI resource = URI.create("gs://" + BUCKET + OBJ);

        String ret = RangerRequestFactory.getObjectDirPath(resource);

        assertThat(ret).isEqualTo(BUCKET + DIR);
    }

    private static void verifyRequest(RangerRequest request, String requiredPermission, String requiredResource) {
        assertThat(request.getUser()).isEqualTo(USER);
        assertThat(request.getUserGroups()).isEqualTo(USER_GROUPS);
        assertThat(request.getResource()).isEqualTo(requiredResource);
        assertThat(request.getActions()).isEqualTo(requiredPermission);
    }
}
