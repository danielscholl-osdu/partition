<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
  > Istio is installed with Istio Ingress Gateway

- Kubernetes cluster version can be checked with the command:

    `kubectl version --short | grep Server`

    The output will be similar to the following:

  ```console
  Server Version: v1.21.11-gke.1100
  ```

- Istio version can be checked in different ways, it is out of scope for this README. You can find more information [here](https://istio.io/latest/docs/setup/install/).

    The following command shows how to check version if Anthos Service Mesh is used:

    `kubectl -n istio-system get pods -lapp=istiod -o=jsonpath='{.items[0].metadata.labels.istio\.io/rev}'`

    The output will be similar to the following:

  ```console
  asm-1132-5
  ```

> It is possible to use other versions, but it hasn't been tested

This example describes installation in **Development mode**:

- In this mode helm chart is installed to the namespace **not labeled with Istio**.
  > More information about labeling can be found [here](https://istio.io/latest/docs/setup/additional-setup/sidecar-injection) (Istio) or [here](https://cloud.google.com/service-mesh/docs/managed/select-a-release-channel#default-injection-labels) (Anthos Service Mesh)

    You can find all labels for your namespace with the command:

     `kubectl get namespace <namespace> -o jsonpath={.metadata.labels}`

    The output shows that there are no any labels related to Istio:

    ```console
    {"kubernetes.io/metadata.name":"default"}
    ```

    When the namespace is labeled with Istio, the output could be:

    ```console
    {"istio-injection":"enabled","kubernetes.io/metadata.name":"default"}
    ```

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)

    Helm version can be checked with the command:

    `helm version --short`

    The output will be similar to the following:

  ```console
  v3.7.1+gd141386
  ```

- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

    Kubectl version can be checked with the command:

    `kubectl version --short | grep Client`

    The output will be similar to the following:

  ```console
  Client Version: v1.21.0
  ```

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**logLevel** | logging level | string | INFO | yes
**springProfilesActive** | active spring profile | string | gcp | yes
**projectId** | your Google Cloud project id | string | - | only in case of Google Cloud installation
**dataProjectId** | in case of multiproject cloud installation (services and data stored in different project) the name of data project | string | - | only in case of multiproject installation
**partitionAdminAccounts** | admin accounts validated by partition service | string | - | yes
**serviceAccountTail** | ending of Google Cloud service account | string | .iam.gserviceaccount.com | yes
**partitionName** | partition host | string | partition | yes
**partitionNamespace** | datastore namespace where partition will store the data | string | partition | yes
**dataPartitionId** | data partition id | string | - | yes
**datafierSa** | datafier service account | string | datafier | yes
**bucketPrefix** | minio bucket name prefix | string | - | only in case of Reference installation
**minioExternalEndpoint** | api url for external minio, if external minio is configured - this value will be set for MINIO_ENDPOINT and FILE_MINIO_ENDPOINT in bootstrap configmap | string | - | no

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**requestsCpu** | amount of requests CPU | string | 0.1 | yes
**requestsMemory** | amount of requests memory | string | 260M | yes
**limitsCpu** | CPU limit | string | 1 | yes
**limitsMemory** | memory limit | string | 1G | yes
**serviceAccountName** | name of your service account | string | partition | yes
**image** | path to the image in a registry | string | - | yes
**imagePullPolicy** | when to pull the image | string | IfNotPresent | yes
**bootstrapImage** | name of the bootstrap image | string | - | yes

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**appName** | name of the app | string | partition | yes
**configmap** | configmap to be used | string | partition-config | yes
**domain** | your domain | string | - | yes
**onPremEnabled** | whether on-prem is enabled | boolean | false | yes
**secret** | secret for postgres | string | partition-postgres-secret | yes
**mtlsMode** | MTLS mode | string | STRICT | yes
**realm** | realm in keycloak | string | osdu | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install gcp-partition-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall gcp-partition-deploy
```

To delete secrets and PVCs:

```console
kubectl delete secret --all; kubectl delete pvc --all
```

[Move-to-Top](#deploy-helm-chart)
