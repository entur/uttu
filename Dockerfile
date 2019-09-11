FROM openjdk:11-jre
ADD target/uttu-*-SNAPSHOT.jar uttu.jar

EXPOSE 8080
CMD java $JAVA_OPTIONS -jar /uttu.jar
