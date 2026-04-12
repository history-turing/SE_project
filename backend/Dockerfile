ARG APP_MODULE=whu-treehole-server
ARG APP_PORT=8080

FROM maven:3.9.9-eclipse-temurin-17 AS builder

ARG APP_MODULE

WORKDIR /workspace

COPY pom.xml settings.xml ./
COPY whu-treehole-common/pom.xml whu-treehole-common/pom.xml
COPY whu-treehole-domain/pom.xml whu-treehole-domain/pom.xml
COPY whu-treehole-infra/pom.xml whu-treehole-infra/pom.xml
COPY whu-treehole-server/pom.xml whu-treehole-server/pom.xml
COPY whu-treehole-message-service/pom.xml whu-treehole-message-service/pom.xml
COPY whu-treehole-gateway/pom.xml whu-treehole-gateway/pom.xml

RUN mvn -q -s settings.xml -pl ${APP_MODULE} -am dependency:go-offline

COPY whu-treehole-common whu-treehole-common
COPY whu-treehole-domain whu-treehole-domain
COPY whu-treehole-infra whu-treehole-infra
COPY whu-treehole-server whu-treehole-server
COPY whu-treehole-message-service whu-treehole-message-service
COPY whu-treehole-gateway whu-treehole-gateway

RUN mvn -q -s settings.xml -pl ${APP_MODULE} -am clean package -DskipTests
RUN find /workspace/${APP_MODULE}/target -maxdepth 1 -name "${APP_MODULE}-*.jar" ! -name "*.jar.original" -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre-alpine

ARG APP_PORT

WORKDIR /app

RUN apk add --no-cache tzdata \
    && addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder /workspace/app.jar /app/app.jar

USER spring

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
