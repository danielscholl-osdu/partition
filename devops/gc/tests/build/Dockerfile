FROM alpine:3.16

RUN apk update && \
    apk -q add jq postgresql-client bash

COPY ./devops/gc/tests/build/ ./opt
RUN chmod +x /opt/bootstrap.sh

WORKDIR /opt

CMD ["/bin/bash", "-c", "/opt/bootstrap.sh"]
