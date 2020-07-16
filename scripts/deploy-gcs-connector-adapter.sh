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

#######################
# Environment Setting #
#######################
# The bucket that will be used as a distribution cache for the ranger-gcs-connector-adapter.jar.
# The built adapter jar and init script will be uploaded to the bucket.
# All the nodes will download the adapter jar from the bucket.
BUCKET_NAME="<bucket-name>"

# The name of the jar in the bucket.
# The built adapter will be renamed to this name after upload to the bucket.
JAR_DST="ranger-gcs-connector-adapter.jar"

# The init script's name in the bucket.
# The init script will be renamed to this name after upload to the bucket.
SCRIPT_DST="download-ranger-gcs-connector-adapter-init-action.sh"

##############################################################################
# GCS environment
PROJECT_ROOT=$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )")

HADOOP_HOME="/usr/lib/hadoop"

MODULE_ADAPTER="ranger-gcs-connector-adapter"
TARGET_DIR="target"

JAR_NAME=$(ls $PROJECT_ROOT/$MODULE_ADAPTER/$TARGET_DIR/ | grep -E "^$MODULE_ADAPTER.*\.jar$")

# Copy to gcs bucket
gsutil cp $PROJECT_ROOT/$MODULE_ADAPTER/$TARGET_DIR/$JAR_NAME gs://$BUCKET_NAME/$JAR_DST

# Deploy init script
TMP_FILE="tmp_init-action.sh"

cp $PROJECT_ROOT/scripts/download-ranger-gcs-connector-adapter-init-action.sh $TMP_FILE

sed -i "s|BUCKET_NAME=\"<bucket-name>\"|BUCKET_NAME=\"$BUCKET_NAME\"|g" $TMP_FILE
sed -i "s|JAR_NAME=\"<jar-name>\"|JAR_NAME=\"$JAR_DST\"|g" $TMP_FILE
sed -i "s|HADOOP_HOME=\"<hadoop-home>\"|HADOOP_HOME=\"$HADOOP_HOME\"|g" $TMP_FILE

gsutil cp $TMP_FILE gs://$BUCKET_NAME/$SCRIPT_DST

rm -f $TMP_FILE
