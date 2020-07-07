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

set -e

# Store xtrace status. Need to restore this after set +x.
ORIGINALXTRACE=$(shopt -po xtrace)

###################################
# Resolve project root directory. #
###################################

PROJECT_ROOT=$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )")

############################################
# Resolve Ranger_HOME and  admin password. #
############################################

# check environment variable, if not present, guess one.
if [[ -z $RANGER_HOME ]] ; then
	RANGER_HOME="/usr/lib/ranger"
fi

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

MODULE="ranger-gcs-service"
TARGET_DIR="target"
SERVICE_DEF_DIR="classes"
SERVICE_DEF="ranger-servicedef-gcs.json"

PLUGIN_NAME="gcs"
JAR_DESTINATION="$RANGER_HOME/ranger-admin/ews/webapp/WEB-INF/classes/ranger-plugins/$PLUGIN_NAME"

echo "Start deploying Ranger service and service definition to Ranger Server."
echo "Copy jar file to Ranger directory"

# depoly jar
mkdir -p $JAR_DESTINATION
JAR_NAME=$(ls $MODULE/$TARGET_DIR | grep jar)
cp $PROJECT_ROOT/$MODULE/$TARGET_DIR/$JAR_NAME $JAR_DESTINATION/

if [[ $? -ne 0 ]] ; then
	echo "Something went wrong when copying $MODULE/$TARGET_DIR/$JAR_NAME to $JAR_DESTINATION."
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

DATA="@$PROJECT_ROOT/$MODULE/$TARGET_DIR/$SERVICE_DEF_DIR/$SERVICE_DEF"
REQ_DESTINATION="http://localhost:6080/service/plugins/definitions"

# upload service def
echo "Making HTTP POST request to $REQ_DESTINATION. User=admin, data=$DATA."

set +x
curl -u $ADMIN -X POST -H "Accept: application/json" -H "Content-Type: application/json" --data $DATA $REQ_DESTINATION
eval $ORIGINALXTRACE

# curl don't change line at the end, and it may mess up prompt badly.
echo -e ""
