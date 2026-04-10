FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml settings.xml ./
COPY whu-treehole-common/pom.xml whu-treehole-common/pom.xml
COPY whu-treehole-domain/pom.xml whu-treehole-domain/pom.xml
COPY whu-treehole-infra/pom.xml whu-treehole-infra/pom.xml
COPY whu-treehole-server/pom.xml whu-treehole-server/pom.xml

RUN mvn -q -s settings.xml -pl whu-treehole-server -am dependency:go-offline

COPY whu-treehole-common whu-treehole-common
COPY whu-treehole-domain whu-treehole-domain
COPY whu-treehole-infra whu-treehole-infra
COPY whu-treehole-server whu-treehole-server

RUN mvn -q -s settings.xml -pl whu-treehole-server -am clean package -DskipTests
RUN find /workspace/whu-treehole-server/target -maxdepth 1 -name "whu-treehole-server-*.jar" ! -name "*.jar.original" -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache tzdata \
    && addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder /workspace/app.jar /app/app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
