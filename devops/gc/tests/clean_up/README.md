# Cleanup Script for Testing Environment

This is bash script used for cleanup temporary resources in Kubernetes cluster and Datastore.
It consists of two parts, which can be called separately or together.

## Prerequisites

* gcloud, authorized with account with sufficient permissions (e.g. Cloud Datastore Owner role). For more information on gcloud-sdk look at [Installing the gcloud CLI Documentation](https://cloud.google.com/sdk/docs/install)
* for cluster part:
  * installed kubectl and jq tools
  * active kubeconfig context, collect necessary credentials before starting the script
* for Datastore part:
  * Python 3.7 and higher
  * It's recommended to use `virtualenv` tool for Python. To install virtualenv on Debian-based Linux distributions you can use `apt-get install python3-venv`.

## Configuration

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

## How to use scenarios

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

> _**NOTE:**_ additional documentation for [Datastore cleanup script](https://community.opengroup.org/osdu/platform/deployment-and-operations/infra-gcp-provisioning/-/blob/master/tools/README.md).

### Python datastore cleanup script

#### Overview

Python script that provides an easy way to cleanup datastore namespaces and kinds.
Furthermore, script supports queries to filter entities that should be deleted.
After running script shows number of deleted entities.

#### Prerequisites for the script

* gcloud, authorized with account with sufficient permissions (e.g. Cloud Datastore Owner role). For more information on gcloud-sdk look at [Installing the gcloud CLI Documentation](https://cloud.google.com/sdk/docs/install)
* Python 3.7 and higher
* It's recommended to use `virtualenv` tool for Python. To install virtualenv on Debian-based Linux distributions you can use `apt-get install python3-venv`.

*This script was tested on Debian-based Linux and WSL with Python 3.7 and higher.*

#### Configuration example

Configuration without gcloud and python3 available will require installation of google-cloud-sdk and Python3.
For more information on google-cloud-sdk look at [Installing the gcloud CLI Documentation](https://cloud.google.com/sdk/docs/install)
For more information on Python3 installation look at [Installing Python](https://www.python.org/downloads/)

```sh
# Authenticate gcloud via a web flow
$ gcloud auth login
# OR using service account key file:
$ gcloud auth activate-service-account --key-file <service-account-key-file>

# Install virtualenv via apt-get
$ apt-get install python3-venv -y -q

# Create virtualenv
$ python3 -m venv venv

# Activate virtualenv
$ source venv/bin/activate

# Install all pypi dependencies
# You need to install them the first time you start using the script
$ pip install -r requirements.txt

```

#### How to use

`-p <project_id>` is required parameter to start script.

```sh
# Get help information about script
$ python datastore_clean_up.py -h

# Delete all records in Datastore
$ python datastore_clean_up.py -p osdu-cicd-epam --delete-all

# Delete all records in Datastore of specific kind and namespace
$ python datastore_clean_up.py -p osdu-cicd-epam -n opendes -k LegalTagHistoric --delete-all

# Delete all records where legaltags values start with "opendes-Test"
# Datastore doesn't support partial-text search, so we need to use these two filters
$ python datastore_clean_up.py -p osdu-cicd-epam -q 'legal.legaltags>="opendes-Test"' -q 'legal.legaltags<"opendes-Tesu"'

# Delete records of specific kind created before 'Jul 22, 2021'
$ python datastore_clean_up.py -p osdu-cicd-epam -k LegalTagHistoric -q 'created<="2021-07-22"'
```

#### *Notes:*

* This script works only on an environment (project) where the datastore API is enabled and the datastore is configured
* Datastore doesn't support partial-text search (similar to SQL LIKE queries), so to achieve similar result - multiple queries with <, >, <=, >= should be used (e.g. `-q 'namespace.kind>="test_0"' -q 'namespace.kind<="test_9"'`)
* Because of complex indexes logic for datastore (by default only simple one-field indexes are created for each entity) filtering is avaliable only within one attribute per script call
* `-p <project-id>` is a required script parameter
* Note the usage of single- and double- quotes for queries.
