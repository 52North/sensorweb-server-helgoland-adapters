FROM maven:3-jdk-8-alpine as BUILD

WORKDIR /app

COPY . ./

RUN mvn --batch-mode --errors --fail-fast \
    --define maven.javadoc.skip=true \
    --define skipTests=true \
    --activate-profiles no-download,dcat \
    install

FROM adoptopenjdk/openjdk8:alpine-slim

WORKDIR /app

COPY --from=BUILD /app/app/target/unpacked/BOOT-INF/lib     /app/lib
COPY --from=BUILD /app/app/target/unpacked/META-INF         /app/META-INF
COPY --from=BUILD /app/app/target/unpacked/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp" ,".:lib/*", "org.n52.helgoland.adapters.Application"]
