FROM google/cloud-sdk:slim

COPY ./provider/partition-gcp/bootstrap/bootstrap_partition.sh ./opt

ENV PREFIX=$(pwd)

RUN chmod 775 /opt/bootstrap_partition.sh

CMD ["/bin/bash", "-c", "source /opt/bootstrap_partition.sh && sleep 365d"]
