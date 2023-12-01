FROM bellsoft/liberica-openjdk-alpine:21.0.1-12
RUN apk update && apk upgrade && apk add --no-cache \
    tini
WORKDIR /deployments
COPY target/uttu-*-SNAPSHOT.jar uttu.jar
RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser
CMD [ "/sbin/tini", "--", "java", "-jar", "uttu.jar" ]