#!/usr/bin/env bash
# # This script performs cleanup operations for Kubernetes and Datastore test environments.
# It provides options to clean up Helm releases in a Kubernetes test namespace,
# clean up Datastore entries, or both. The Helm releases to be deleted are filtered
# based on a configurable variable GC_TEST_HELM_NAME, which defaults to "partition".

# Configurable variable
GC_TEST_HELM_NAME=${GC_TEST_HELM_NAME:-partition}

cluster_cleanup() {
  # Check if the namespace contains the word "test"
  if [[ "$GC_HELM_NAMESPACE" == *test ]]
  then
    # List all Helm releases in the namespace and uninstall those containing the value of GC_TEST_HELM_NAME
    LIST=$(helm -n $GC_HELM_NAMESPACE list -o json | jq -r .[].name)
    for RELEASE in $LIST
    do
      if [[ "$RELEASE" == *"$GC_TEST_HELM_NAME"* ]]; then
        helm -n $GC_HELM_NAMESPACE uninstall $RELEASE
      fi
    done
    echo "Cluster cleanup Finished"
  else
    echo "Production namespace, no operations will be perfomed"
    exit 1
  fi

}

datastore_cleanup () {
  # Install necessary packages and Python dependencies
  apk add py3-pip python3-dev libffi-dev
  pip install --upgrade pip
  pip install --upgrade setuptools
  pip install -q -r devops/gc/tests/clean_up/requirements.txt

  # Run the datastore cleanup script
  python3 devops/gc/tests/clean_up/datastore_clean_up.py -p "$GC_PROJECT" -n $PARTITION_NAMESPACE --delete-all

  echo "Datastore cleanup finished"
}

# Enable script debugging and exit immediately if a command exits with a non-zero status
set -ex

case "$1" in
  --cluster)
    echo "K8s test namespace cleanup will be perfomed"
    cluster_cleanup;;
  --datastore)
    echo "Datastore test namespace cleanup will be perfomed"
    datastore_cleanup;;
  --all)
    echo "Test resources in k8s and Datastore will be deleted "
    cluster_cleanup
    datastore_cleanup;;
  *)
    echo "You must set up an option like --cluster, --datastore, or --all"
    exit 1;;
esac
