FROM adoptopenjdk/openjdk11:jre-11.0.11_9-alpine
WORKDIR /deployments
COPY target/uttu-*-SNAPSHOT.jar uttu.jar
RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
USER appuser
CMD java $JAVA_OPTIONS -jar uttu.jar