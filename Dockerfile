# ===========================================================================
#  Dockerfile - a recipe for building a self-contained CONTAINER IMAGE of the
#  app. Railway (and any container host) reads this to build and run us.
#
#  It uses TWO stages:
#    1. "build"  - a big image WITH Maven+JDK that compiles our jar.
#    2. runtime  - a small image with ONLY a Java runtime + the finished jar.
#  Shipping only stage 2 keeps the deployed image small and fast to boot.
# ===========================================================================

# ---- Stage 1: build the jar ------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy just the pom first so Docker can CACHE the dependency download layer:
# it only re-runs when pom.xml changes, not on every source edit.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the source and build. Skip tests here (CI already runs them) so the
# deploy build stays fast; drop "-DskipTests" if you want tests to gate deploys.
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: the lean runtime image --------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat jar produced by stage 1 (wildcard = version-independent).
COPY --from=build /app/target/*.jar app.jar

# Railway sets $PORT; our application.properties already reads it. EXPOSE is
# documentation - the platform maps the real port for us.
EXPOSE 8080

# Start the Spring Boot app.
ENTRYPOINT ["java", "-jar", "app.jar"]
