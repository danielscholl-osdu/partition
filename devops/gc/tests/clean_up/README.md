## Cleanup Script for Testing Environment

This is bash script used for cleanup temporary resources in Kubernetes cluster and Datastore.
It consists of two parts, which can be called separately or together.

#### **Prerequisites**

* gcloud, authorized with account with sufficient permissions (e.g. Cloud Datastore Owner role). For more information on gcloud-sdk look at [Installing the gcloud CLI Documentation](https://cloud.google.com/sdk/docs/install)
* for cluster part:
  * installed kubectl and jq tools
  * active kubeconfig context, collect necessary credentials before starting the script
* for Datastore part:
  * Python 3.7 and higher
  * It's recommended to use `virtualenv` tool for Python. To install virtualenv on Debian-based Linux distributions you can use `apt-get install python3-venv`.

#### **Configuration**

[How to install gcloud CLI](https://cloud.google.com/sdk/docs/install)
[How to install Python](https://www.python.org/downloads/)

**Common steps:**

Provide credentials for gcloud:

```sh
# Interactive
gcloud auth login

# Or using service account
gcloud auth activate-service-account --key-file <path-to-sa-json>

```

**Cluster cleanup:**

```sh
# Get cluster credentials
gcloud container clusters get-credentials <cluster-name> --zone <cluster-zone> OR --region <cluster-region>

```

**Datastore cleanup:**

```sh
# Setup redentials for script
gcloud auth application-default login
# OR
export GOOGLE_APPLICATION_CREDENTIALS=<path-to-sa-json>

# Install virtualenv via apt-get
$ apt-get install python3-venv -y -q

# Create virtualenv
$ python3 -m venv venv

# Activate virtualenv
$ source venv/bin/activate

# Install all pypi dependencies
$ pip install -r requirements.txt

```

#### **How to use scenarios**

**Cluster cleanup:**

* Define variable _GC_HELM_NAMESPACE_ and start the script with _--cluster_ key:

```sh
export GC_HELM_NAMESPACE=<test-namespace> # k8s namespace where test resources deployed
./clean_up.sh --cluster

```

**Datastore cleanup:**

* Define variables _GC_PROJECT_ and _PARTITION_NAMESPACE_, and start the script with _--datastore_ key:

```sh
export GC_PROJECT=<project-id> # Google project id where Datastore used
export PARTITION_NAMESPACE=<test namespace in Datastore> # test namespace in Datastore
./clean_up.sh --datastore

```

**Cleanup all test resources:**

* Define variables for both cases and start script with _--all_ key:

```sh
export GC_HELM_NAMESPACE=<test-namespace>
export GC_PROJECT=<project-id>
export PARTITION_NAMESPACE=<test namespace in Datastore>
./clean_up.sh --all

```

> ***NOTE:*** additional documentation for [Datastore cleanup script](https://community.opengroup.org/osdu/platform/deployment-and-operations/infra-gcp-provisioning/-/blob/master/tools/README.md).
