FROM alpine

WORKDIR /opt

COPY ./provider/partition-gcp/bootstrap/ /opt/

ENV PREFIX=$(pwd)

RUN apk update && apk add \
    bash \
    curl \
    && chmod 775 bootstrap_partition.sh

CMD ["/bin/bash", "-c", "./bootstrap_partition.sh && sleep 365d"]
