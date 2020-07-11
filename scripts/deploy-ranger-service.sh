#!/bin/bash

# Copyright 2020 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Store xtrace status. Need to restore this after set +x.
ORIGINALXTRACE=$(shopt -po xtrace)

# Exit if error
set -e

PROJECT_ROOT=$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )")
source $PROJECT_ROOT/scripts/project-settings.sh

##################################
# Resolve Ranger admin password. #
##################################

# get password
if [[ -f ${RANGER_HOME}/ranger-admin/install.properties ]] ; then
	set +x;
	ADMINPASSWD=$(grep "rangerAdmin_password" "${RANGER_HOME}/ranger-admin/install.properties" | cut -b 22-);
	eval $ORIGINALXTRACE
else
	echo "Can't find '${RANGER_HOME}/ranger-admin/install.properties', please check RANGER_HOME environment variable."
	exit 0
fi

#################################
# Copy jar to ranger class path #
#################################

SERVICE_DEF_DIR="classes"
SERVICE_DEF="ranger-servicedef-gcs.json"

JAR_DESTINATION="$RANGER_HOME/ranger-admin/ews/webapp/WEB-INF/classes/ranger-plugins/gcs"

echo "Start deploying Ranger service and service definition to Ranger Server."
echo "Copy jar file to Ranger directory"

# depoly jar
mkdir -p $JAR_DESTINATION
JAR_NAME=$(ls $MODULE_SERVICE/$TARGET_DIR | grep jar)
cp $PROJECT_ROOT/$MODULE_SERVICE/$TARGET_DIR/$JAR_NAME $JAR_DESTINATION/

if [[ $? -ne 0 ]] ; then
	echo "Something went wrong when copying $MODULE_SERVICE/$TARGET_DIR/$JAR_NAME to $JAR_DESTINATION."
	exit 1
fi

chown -R ranger $JAR_DESTINATION/
chgrp -R ranger $JAR_DESTINATION

##############################################
# Upload service definition to Ranger server #
##############################################

echo "Upload service definition to Ranger server."

# ranger settings
set +x
ADMIN="admin:$ADMINPASSWD";
eval $ORIGINALXTRACE

DATA="@$PROJECT_ROOT/$MODULE_SERVICE/$TARGET_DIR/$SERVICE_DEF_DIR/$SERVICE_DEF"
REQ_DESTINATION="$RANGER_HOST:$RANGER_PORT/service/plugins/definitions"

# upload service def
echo "Making HTTP POST request to $REQ_DESTINATION. User=admin, data=$DATA."

set +x
curl -u $ADMIN -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data $DATA $REQ_DESTINATION
eval $ORIGINALXTRACE

# curl don't change line at the end, and it may mess up prompt badly.
echo -e ""

#############################################
# Create a default service on Ranger server #
#############################################

DATA=$(cat <<-EOD
        {
                "configs": {},
                "description": "GCS service",
                "isEnabled": true,
                "name": "$DEFAULT_SERVICE_NAME",
                "type": "GCS",
                "version": 1
        }
EOD
)

SERVICE_API="$RANGER_HOST:$RANGER_PORT/service/public/v2/api/service"

echo "Creating default GCS service. Name=gcs."

set +x
curl -u $ADMIN -X POST -H "Accept: application/json" -H "Content-Type: application/json"  --data "$DATA" $SERVICE_API
eval $ORIGINALXTRACE

# curl don't change line at the end, and it may mess up prompt badly.
echo -e ""
