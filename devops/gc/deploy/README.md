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

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain | string | - | yes
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | `true` | yes
**global.dataPartitionId** | data partition id | string | - | yes
**global.logLevel** | severity of logging level | string | `ERROR` | yes
**global.tier** | Only PROD must be used to enable autoscaling | string | "" | no
**global.autoscaling** | enables horizontal pod autoscaling, when tier=PROD | boolean | true | yes

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging severity level for this service only  | string | - | yes, only if differs from the `global.logLevel`
**data.projectId** | your Google Cloud project id | string | - | yes
**data.dataProjectId** | in case of multiproject cloud installation (services and data stored in different project) the name of data project | string | - | only in case of multiproject installation
**data.partitionHost** | partition host | string | `partition` | yes
**data.partitionNamespace** | datastore namespace where partition will store the data | string | `partition` | yes
**data.datafierSa** | datafier service account | string | `datafier` | yes
**data.indexerAugmenterEnabled** | enable indexer Augmenter | string | `false` | no
**data.elasticHttps** | Configuration of the communication protocol for search and indexer services | boolean | - | yes |

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requests CPU | string | `10m` | yes
**data.requestsMemory** | amount of requests memory | string | `400Mi` | yes
**data.limitsCpu** | CPU limit | string | `500m` | only if `global.limitsEnabled` is true
**data.limitsMemory** | memory limit | string | `1G` | only if `global.limitsEnabled` is true
**data.serviceAccountName** | name of your service account | string | `partition` | yes
**data.image** | path to the image in a registry | string | - | yes
**data.imagePullPolicy** | when to pull the image | string | `IfNotPresent` | yes
**data.bootstrapImage** | name of the bootstrap image | string | - | yes

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.appName** | name of the app | string | `partition` | yes
**conf.configmap** | configmap to be used | string | `partition-config` | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `500m` | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | `100Mi` | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `512Mi` | yes
**istio.bootstrapProxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.bootstrapProxyCPULimit** | CPU limit for Envoy sidecars | string | `100m` | yes
**istio.sidecarInject** | whether Istio sidecar will be injected. Setting to `false` reduces security, because disables authorization policy. | boolean | `true` | yes
**istio.partitionEditors** | list of users who can edit partitions. If it is empty, the service allows all external GET requests and POST/PUT/PATCH for bootstrap k8s service account. It accepts gc accounts only | list | - | no

### Horizontal Pod Autoscaling (HPA) variables (works only if tier=PROD and autoscaling=true)

| Name                                                | Description                                                                   | Type    | Default          | Required                                                       |
|-----------------------------------------------------|-------------------------------------------------------------------------------|---------|------------------|----------------------------------------------------------------|
| **hpa.minReplicas**                                 | minimum number of replicas                                                    | integer | `3`              | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.maxReplicas**                                 | maximum number of replicas                                                    | integer | `15`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.targetType**                                  | type of measurements: AverageValue or Value                                   | string  | `"AverageValue"` | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.targetValue**                                 | threshold value to trigger the scaling up                                     | integer | `400`            | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleUpStabilizationWindowSeconds**   | time to start implementing the scale up when it is triggered                  | integer | `10`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleUpPoliciesValue**                | the maximum number of new replicas to create (in percents from current state) | integer | `50`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleUpPoliciesPeriodSeconds**        | pause for every new scale up decision                                         | integer | `15`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleDownStabilizationWindowSeconds** | time to start implementing the scale down when it is triggered                | integer | `60`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleDownPoliciesValue**              | the maximum number of replicas to destroy (in percents from current state)    | integer | `25`             | only if `global.autoscaling` is true and `global.tier` is PROD |
| **hpa.behaviorScaleDownPoliciesPeriodSeconds**      | pause for every new scale down decision                                       | integer | `60`             | only if `global.autoscaling` is true and `global.tier` is PROD |

### Limits variables

| Name                     | Description                                     | Type    | Default | Required                                                       |
|--------------------------|-------------------------------------------------|---------|---------|----------------------------------------------------------------|
| **limits.maxTokens**     | maximum number of requests per fillInterval     | integer | `300`   | only if `global.autoscaling` is true and `global.tier` is PROD |
| **limits.tokensPerFill** | number of new tokens allowed every fillInterval | integer | `300`   | only if `global.autoscaling` is true and `global.tier` is PROD |
| **limits.fillInterval**  | time interval                                   | string  | `"1s"`  | only if `global.autoscaling` is true and `global.tier` is PROD |

### Methodology for Parameter Calculation variables: **hpa.targetValue**, **limits.maxTokens** and **limits.tokensPerFill**

The parameters **hpa.targetValue**, **limits.maxTokens** and **limits.tokensPerFill** were determined through empirical testing during load testing. These tests were conducted using the N2D machine series, which can run on either AMD EPYC Milan or AMD EPYC Rome processors. The values were fine-tuned to ensure optimal performance under typical workloads.

### Recommendations for New Instance Types

When changing the instance type to a newer generation, such as the C3D series, it is essential to conduct new load testing. This ensures the parameters are recalibrated to match the performance characteristics of the new processor architecture, optimizing resource utilization and maintaining application stability.

### Install the helm chart

Run this command from within this directory:

```console
helm install gc-partition-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall gc-partition-deploy
```

To delete secrets and PVCs:

```console
kubectl delete secret --all; kubectl delete pvc --all
```

[Move-to-Top](#deploy-helm-chart)
