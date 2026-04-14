FROM gradle:8.5-jdk17 AS build

WORKDIR /app

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=${GITHUB_ACTOR}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# 멀티모듈 의존성 캐시를 위해 빌드 설정부터 복사
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle settings.gradle gradle.properties ./
COPY common/build.gradle common/build.gradle
COPY app/build.gradle app/build.gradle

RUN chmod +x gradlew
RUN ./gradlew :app:dependencies :common:dependencies --no-daemon

# 전체 프로젝트 복사
COPY . .

# 애플리케이션 빌드
RUN ./gradlew :app:build --no-daemon -x test

# 2단계: 실행 스테이지
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app

COPY --from=build /app/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=dev"]
