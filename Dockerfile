FROM openjdk:11-jre
WORKDIR /deployments
COPY target/uttu-*-SNAPSHOT.jar uttu.jar
CMD java $JAVA_OPTIONS -jar uttu.jar