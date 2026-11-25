FROM gcr.io/distroless/java25-debian13:nonroot@sha256:4618e7a1a1f5177222985347e1ffd503076f6be5837b89c8fd02800bd8551bde AS builder
WORKDIR /home/nonroot
COPY target/uttu-*.jar application.jar
RUN [ "/usr/bin/java", "-Djarmode=tools", "-jar", "application.jar", "extract", "--layers", "--destination", "extracted" ]

FROM gcr.io/distroless/java25-debian13:nonroot@sha256:4618e7a1a1f5177222985347e1ffd503076f6be5837b89c8fd02800bd8551bde
WORKDIR /deployments
COPY --from=builder --chown=0:0 /home/nonroot/extracted/dependencies/ ./
COPY --from=builder --chown=0:0 /home/nonroot/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=0:0 /home/nonroot/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=0:0 /home/nonroot/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT [ "/usr/bin/java", "-jar", "application.jar" ]
