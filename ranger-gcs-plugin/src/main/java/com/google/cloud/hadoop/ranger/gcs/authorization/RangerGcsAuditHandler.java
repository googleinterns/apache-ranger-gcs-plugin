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
import org.apache.ranger.audit.model.AuthzAuditEvent;
import org.apache.ranger.plugin.audit.RangerDefaultAuditHandler;
import org.apache.ranger.plugin.policyengine.RangerAccessRequest;
import org.apache.ranger.plugin.policyengine.RangerAccessResource;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Auditing handler. Ranger policy engine will audit results to this class.
 */
public class RangerGcsAuditHandler extends RangerDefaultAuditHandler {
    private static final Log LOG = LogFactory.getLog(RangerGcsAuditHandler.class);

    private static final String GCS_DOMAIN = "gs://";
    private static final String PATH_SEPARATER = "/";

    private AuthzAuditEvent auditEvent = null;
    private final String gcsurl;
    private final String action;

    public RangerGcsAuditHandler(String bucket, String path, List<String> actions) {
        this.gcsurl = new StringBuilder(GCS_DOMAIN)
                .append(bucket)
                .append(PATH_SEPARATER)
                .append(path)
                .toString();
        this.action = String.join(", ", actions);
    }

    /**
     * Prepare an event for auditing.
     * Combine several events into one audit events.
     *
     * This method does NOT write event to audit log.
     */
    @Override
    public void processResult(RangerAccessResult result) {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsAuditHandler.logAudit(" + result + ")");

        if (auditEvent == null)
            auditEvent = super.getAuthzEvents(result);

        // Combine new result with previous results
        if (auditEvent != null) {
            RangerAccessRequest request = result.getAccessRequest();
            RangerAccessResource resource = request.getResource();
            String resourcePath = resource != null ? resource.getAsString() : null;

            auditEvent.setEventTime(request.getAccessTime() != null ? request.getAccessTime() : new Date());
            auditEvent.setResultReason(resourcePath);

            auditEvent.setAccessResult((short) (result.getIsAllowed() ? 1 : 0));
            auditEvent.setPolicyId(result.getPolicyId());

            Set<String> tags = getTags(request);
            if (tags != null) {
                auditEvent.setTags(tags);
            }
        }

        if(LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsAuditHandler.logAudit(" + result + "): " + auditEvent);
    }

    /**
     * Actually write event to audit log.
     */
    public void flush() {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsAuditHandler.logAudit(" + auditEvent + ")");

        // undetermined events may not have audit log
        if (auditEvent != null) {
            // override some information
            auditEvent.setResourcePath(gcsurl);
            auditEvent.setAccessType(action);
            auditEvent.setAction(action);

            super.logAuthzAudit(auditEvent);
        }

        if(LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsAuditHandler.logAudit(" + auditEvent + ")" );
    }
}
