FROM openjdk:8-jre

COPY build/install/send-letter-producer-consumer /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8486/health

EXPOSE 8486

ENTRYPOINT ["/opt/app/bin/send-letter-producer-consumer"]
