FROM gcr.io/distroless/java25-debian13:nonroot@sha256:59bbb349c9752fc57b75744b41b1614c9dabe8bb4869523aabd5a4288f6c09a0 AS builder
WORKDIR /home/nonroot
COPY target/uttu-*.jar application.jar
RUN [ "/usr/bin/java", "-Djarmode=tools", "-jar", "application.jar", "extract", "--layers", "--destination", "extracted" ]

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:59bbb349c9752fc57b75744b41b1614c9dabe8bb4869523aabd5a4288f6c09a0
WORKDIR /deployments
COPY --from=builder /home/nonroot/extracted/dependencies/ ./
COPY --from=builder /home/nonroot/extracted/spring-boot-loader/ ./
COPY --from=builder /home/nonroot/extracted/snapshot-dependencies/ ./
COPY --from=builder /home/nonroot/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT [ "/usr/bin/java", "-jar", "application.jar" ]
