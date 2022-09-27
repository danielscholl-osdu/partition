#!/bin/sh

mkdir /tmp/cm-config

export HOME=/tmp/cm-config

cd /tmp/cm-config

COUCH_SECRET_NAME=$(oc get secrets |grep Opaque| grep couchdb| awk '{ print $1 }')

VALUE_COUCH_SECRET=$(oc get secret $COUCH_SECRET_NAME -o jsonpath="{ .data.adminPassword}" |base64 -d)

oc patch cm partition-config -p "{\"data\": {\"ibm.db.password\": \"$VALUE_COUCH_SECRET\"}}"

KEYCLOAK_LB_IP=$(oc get svc keycloak-discovery-lb -o jsonpath="{ .status.loadBalancer.ingress[0].ip}")

KEYCLOAK_HOSTNAME=$(oc get svc keycloak-discovery-lb -o jsonpath="{ .status.loadBalancer.ingress[0].hostname}")

KEYCLOAK_LB_IP=${KEYCLOAK_LB_IP:=$KEYCLOAK_HOSTNAME}

oc patch cm partition-config -p "{\"data\": {\"keycloak.auth-server-url\": \"http://$KEYCLOAK_LB_IP/auth\"}}"

oc patch cm partition-config -p "{\"data\": {\"ibm.keycloak.endpoint_url\": \"http://$KEYCLOAK_LB_IP\"}}"

oc patch cm partition-config -p "{\"data\": {\"partition.keycloak.url\": \"$KEYCLOAK_LB_IP\"}}"

MINIO_LB_IP=$(kubectl get svc osdu-deploy-minio -o jsonpath="{ .status.loadBalancer.ingress[0].ip}")

MINIO_LB_HOSTNAME=$(kubectl get svc osdu-deploy-minio -o jsonpath="{ .status.loadBalancer.ingress[0].hostname}")

MINIO_LB_PORT=$(kubectl get svc osdu-deploy-minio -o=jsonpath="{.spec.ports[?(@.name=='minio-api')].port}")

MINIO_LB_PORT=${MINIO_LB_PORT:=9000}

MINIO_LB_IP=${MINIO_LB_IP:=$MINIO_LB_HOSTNAME}

kubectl patch cm partition-config -p "{\"data\": {\"ibm.cos.endpoint_url\": \"http://$MINIO_LB_IP:$MINIO_LB_PORT\"}}"

oc patch RequestAuthentication core-request-authn  --type json   -p='[{"op": "replace", "path": "/spec/jwtRules/0/issuer", "value":"http://'$KEYCLOAK_LB_IP'/auth/realms/OSDU"}]'

echo "Waiting for Keycloak Server to come up"

while [[ "$(curl -s -L -o /dev/null -w ''%{http_code}'' http://keycloak-discovery:8080/auth/realms/OSDU/protocol/openid-connect/certs)" != "200" ]]; do sleep 5; done

sleep 10

KEYCLOAK_CERT=$(curl http://keycloak-discovery:8080/auth/realms/OSDU/protocol/openid-connect/certs)

oc get RequestAuthentication core-request-authn -o yaml | sed "s|jwksUri: *.*|jwks: ''|g" |  oc apply -f -

oc get RequestAuthentication core-request-authn -o yaml | sed "s|jwks: *.*|jwks: '$KEYCLOAK_CERT'|g" |  oc apply -f -

oc get EnvoyFilter header-2-add-user-from-keycloak-token -o yaml | sed "s/x.x.x.x/$KEYCLOAK_LB_IP/g" | oc apply -f -

oc wait --for=condition=complete --timeout=120s job/amq-pvc-permissions | oc get ActiveMQArtemis ex-aao -o yaml | sed "s/size: 0/size: 1/g" | oc apply -f -
