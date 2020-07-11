**This is not an officially supported Google product.**

# Apache Ranger GCS Plugin

This project implement a proof of concept permission check server using Apache Ranger.

### Modules

### Ranger GCS Service

wip.

### Ranger GCS Plugin

wip.

### Permission Check Service

wip.

### GCS Connector Adapter

wip.


# Usage
## Build the Project

Use the maven or maven wrapper to build.
```
./mvnw clean package
```

The project requires java version 8.

Artifacts will appear in each sub modules' target directory.

## Configure Environment and Deployment Settings

The settings are in ```scripts/project-settings.sh```.
Some default environment settings only work on Dataproc clusters.

1. **RANGER_HOME** should point to the base directory of Ranger's installation.

You can set **RANGER_HOME** environment variable with the below export command.
If the variable is not set, the default installation location ```/usr/lib/ranger``` will be used.
```
export RANGER_HOME=<your ranger installation directory>
```

2. **RANGER_HOST** should be the url of Ranger admin server.

For example, it is ```http://<your-cluster-name>-m``` on Dataproc clusters.

3. **RANGER_PORT** is the port of Ranger server.

The default port is ```6080```.


## Register Ranger GCS Service to Apache Ranger Server

There must be an up-and-running Apache Ranger Server.
If you're using Google Cloud Dataproc, you can install the [Ranger optional component](https://cloud.google.com/dataproc/docs/concepts/components/ranger).

The script bellow will register the Ranger GCS service to the Ranger server.
The script will create a GCS service on Ranger server. You can change the service name in ```scripts/project-settings.sh```.
```
sudo bash ./scripts/deploy-ranger-service.sh
```

### Create/rename a GCS service on Ranger Admin App after deployment

The deployment script creates a service called "gcs".

The name of the GCS service must match the name defined in ```ranger-gcs-security.xml``` for the Ranger plugin.
You can find the setting under ```ranger.plugin.gcs.service.name``` property.
The file is at ```/etc/gcs/conf/ranger-gcs-security.xml``` after deployment of the Ranger plugin proxy server
(as described in the next sections).

## Deploy the Ranger Plugin Proxy Server

### Deploy Configuration Files

Use the following script to deploy configuration files.
```
sudo bash scripts/deploy-ranger-plugin.sh
```

Alternatively, use the following commands to depoly the configuration files manaully.

<details>
    <summary>Click to expand.</summary>

1. Copy configurations to ```/etc/gcs/conf```.

```
sudo mkdir -p /etc/gcs/conf
sudo cp ranger-gcs-plugin/conf/* /etc/gcs/conf
sudo cp ./ranger-gcs-permission-check-service/conf/* /etc/gcs/conf
```

2. Change ip and port of the proxy server in ```/etc/gcs/conf/ranger-gcs-permission-check-service.xml```.

3. Modify the value of ```ranger.plugin.gcs.policy.rest.url``` in ```/etc/gcs/conf/ranger-gcs-security.xml```.

	Replace the string ```policymanagerhost``` with Ranger server's host name and ```port``` with Ranger admin port (The default port number is 6080).

4. Modify the value of audit destination in ```/etc/gcs/conf/ranger-gcs-audit.xml```.

	Replace the string ```solrhosturl``` (under the ```xasecure.audit.destination.solr.urls``` property) with actual solr host.
	Dataproc clusters uses solr to store Ranger audit log.
	Change it to master node's host name if you're using this on Dataproc clusters.

</details>

### Run the Proxy Service

Use the following script to deploy the Ranger plugin proxy service as a systemd service.
```
sudo bash scripts/deploy-proxy-server-as-service.sh
```

Reload systemd services.
```
sudo systemctl daemon-reload
```

After reloading, you can use *systemctl* commands like start, stop and restart to control the service.
```
sudo systemctl start ranger-gcs-plugin-proxy-server.service
```

### (Optional) Enable Log4j

Put a ```log4j.properties``` to ```/etc/gcs/conf```.
You can use the provided setting with "info" log level.
```
sudo cp scripts/log4j.properties /etc/gcs/conf
```

## Create Policy

A GCS policy requires two resource: bucket name and object path.

1. Bucket name is the name of GCS bucket.

2. Object path is an **absolute path** (always start with a "/") from the bucket's root.

Becareful that any non-absolute path (not starting with a "/") may not be matched by the Ranger policy engine.

## Resources

- [Dataproc Ranger Component](https://cloud.google.com/dataproc/docs/concepts/components/ranger)
- [Google Cloud Dataproc - Hadoop Connectors](https://github.com/GoogleCloudDataproc/hadoop-connectors)
