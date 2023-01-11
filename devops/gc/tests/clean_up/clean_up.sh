#!/usr/bin/env bash

cluster_cleanup() {
  # check variable for namespace
  if [[ "$GC_HELM_NAMESPACE" == *test ]]
  then
    # uninstall partition helm releases
    LIST=$(helm -n $GC_HELM_NAMESPACE list -o json | jq -r .[].name)
    for RELEASE in $LIST
    do
      helm -n $GC_HELM_NAMESPACE uninstall $RELEASE
    done
    echo "Cluster cleanup Finished"
  else
    echo "Production namespace, no operations will be perfomed"
    exit 1
  fi

}

datastore_cleanup () {

  apk add py3-pip python3-dev libffi-dev
  pip install --upgrade pip
  pip install --upgrade setuptools
  pip install -q -r devops/gc/tests/clean_up/requirements.txt
  python3 devops/gc/tests/clean_up/datastore_clean_up.py -p "$GC_PROJECT" -n $PARTITION_NAMESPACE --delete-all

  echo "Datastore cleanup finished"
}

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
    echo "You must setup option like --cluster/datastore/all"
    exit 1;;
esac
