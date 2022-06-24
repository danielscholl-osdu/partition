FROM google/cloud-sdk:slim

WORKDIR /opt

COPY ./provider/partition-gcp/bootstrap/ /opt/

ENV PREFIX=$(pwd)

RUN chmod 775 bootstrap_partition.sh

CMD ["/bin/bash", "-c", "./bootstrap_partition.sh && sleep 365d"]
