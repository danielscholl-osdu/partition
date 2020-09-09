#!/bin/bash

set -e

export imageName="redis-partition-cluster"
export tag="v1"

docker login -u ${dockerId} -p ${dockerPassword} ${dockerId}.azurecr.io
docker build -t ${dockerId}.azurecr.io/${imageName}:${tag} .
echo 'Image built'

docker push ${dockerId}.azurecr.io/${imageName}
docker tag ${dockerId}.azurecr.io/${imageName}:${tag} ${dockerId}.azurecr.io/${imageName}:latest

echo 'Added ${dockerId}.azurecr.io/${imageName}:latest tag to ${dockerId}.azurecr.io/${imageName}:${tag}'
docker push ${dockerId}.azurecr.io/${imageName}:${tag}

echo 'Pushing ${dockerId}.azurecr.io/${imageName}:${tag}'
docker push ${dockerId}.azurecr.io/${imageName}:latest
echo 'Pushed ${dockerId}.azurecr.io/${imageName}:latest'