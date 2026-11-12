# Testing Strategy

## Goal

Keep the three services (resource-service, resource-processor, song-service) stable when they change. Each test type checks a different risk: business rules, persistence, HTTP contracts, messaging, and the full cross-service flow.

## Test types

| Type | Tooling | Where | What it verifies | Status |
|---|---|---|---|---|
| Unit | JUnit 5, Mockito | song-service `src/test/java/.../service` | Business rules in SongService: duplicates, not found, CSV parsing, length limit, invalid IDs | Done |
| Integration | `@DataJpaTest`, H2 | song-service `src/test/java/.../repository` | JPA mapping and repository operations | Done |
| Component, HTTP slice | `@WebMvcTest`, MockMvc | song-service `src/test/java/.../controller` | Status codes, validation, error mapping | Done |
| Component, business | Cucumber, `@SpringBootTest`, H2 | song-service `src/test/resources/features` | Create (201), duplicate (409), update (200), delete (204), not found (404) | Done |
| Contract, HTTP | Spring `MockRestServiceServer`, MockMvc | all three services | Request shape and status codes on both sides of each HTTP call | Done |
| Contract, messaging | Mockito | resource-service, resource-processor | Message body type (Integer resource ID) on the RabbitMQ queue | Done |
| End-to-end, cross-service | Cucumber, REST Assured, Awaitility | `e2e-tests` module | Upload, processing through RabbitMQ, metadata in song-service, delete propagation | Done, requires `docker compose up` |

## Contracts

| Contract | Consumer test | Provider test |
|---|---|---|
| resource-service to song-service: `DELETE /songs?id=1,2` returns 204 | resource-service `SongServiceContractTest` | song-service `SongDeleteContractTest` |
| resource-processor to song-service: `POST /songs` with `SongDto` JSON | resource-processor `ResourceProcessorContractTest` | song-service `SongControllerComponentTest` |
| resource-processor to resource-service: `GET /resources/{id}` returns mp3 bytes | resource-processor `ResourceProcessorContractTest` | resource-service `ResourceControllerContractTest` |
| resource-service to RabbitMQ `resource-processing-queue`: Integer resource ID | resource-service `MessageContractProducerTest` | resource-processor `ResourceProcessorContractTest` |

Limitations:
- Contracts are not stored as shared files. Each side checks its own copy of the expectations, so a change on one side is caught only when that side's tests run.
- Pact and Spring Cloud Contract were considered but not used. The current approach uses Spring test tools only and avoids a second framework.

## End-to-end scenario

The `e2e-tests` module runs against the stack from `docker compose up -d --build`:
1. Upload `downloaded.mp3` to resource-service and wait until song-service returns metadata (up to 30 seconds).
2. Delete the resource and wait until song-service returns 404 (up to 30 seconds).

## Defects found by the tests

- song-service was not registered in Eureka because `spring-cloud-starter-netflix-eureka-client` was missing from its `pom.xml`. resource-service could not resolve `song-service` through its load balancer, so metadata was not deleted. The E2E delete scenario caught this.
- resource-service swallowed exceptions from the song-service call and still returned 200. Now the call runs before the local delete. If song-service fails, the error propagates and the resource is kept.

## Running the tests

    cd song-service && mvn clean test
    cd resource-service && mvn clean test
    cd resource-processor && mvn clean test
    docker compose up -d --build
    cd e2e-tests && mvn clean test
