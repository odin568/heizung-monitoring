FROM eclipse-temurin:17-jdk AS builder
WORKDIR application
COPY build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM eclipse-temurin:17-jre
LABEL maintainer="odin568"
EXPOSE 8080
ENV TZ=Europe/Berlin

# Create user/group
RUN groupadd --gid 1000 appgroup && \
    useradd -rm -d /home/appuser -s /bin/bash -g appgroup -G sudo -u 1000 appuser

# Create and own directory
RUN mkdir application && chown -R appuser:appgroup ./application
USER appuser
WORKDIR application

# Copy application from builder stage
COPY --chown=appuser:appgroup --from=builder application/dependencies/ ./
COPY --chown=appuser:appgroup --from=builder application/spring-boot-loader/ ./
COPY --chown=appuser:appgroup --from=builder application/snapshot-dependencies/ ./
COPY --chown=appuser:appgroup --from=builder application/application/ ./

# Set entrypoint
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]