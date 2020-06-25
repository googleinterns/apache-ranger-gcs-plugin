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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService.ticketHandler;

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;

/**
 * Thicket handler interface.
 * All handlers should implement the interface.
 *
 * Handlers can update/modify the ticket directly.
 */
public interface TicketHandler {
    public void handle(RequestTicket ticket);
}
