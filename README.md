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

## Build the Project

Use the maven or maven wrapper to build.
```
./mvnw clean package
```

The project requires java version 8.

Artifacts will appear in each sub modules' target directory.

## Register Ranger GCS Service to Apache Ranger Server

There must be an up-and-running Apache Ranger Server at localhost.
If you're using Google Cloud Dataproc, you can install the [Ranger optional component](https://cloud.google.com/dataproc/docs/concepts/components/ranger).

The scripts bellow will register the Ranger GCS service to the Ranger server.

1. Set environment variable for **RANGER_HOME**. 
```
export RANGER_HOME=<your ranger installation directory>
```
If **RANGER_HOME** environment variable is not set, the default installation location "/usr/lib/ranger" will be used.

2. Execute the depolyment script.
```
sudo bash ./scripts/deploy-ranger-service.sh
```

## Execute Permission Check Service

wip.


## Resources

- [Dataproc Ranger Component](https://cloud.google.com/dataproc/docs/concepts/components/ranger)
- [Google Cloud Dataproc - Hadoop Connectors](https://github.com/GoogleCloudDataproc/hadoop-connectors)
