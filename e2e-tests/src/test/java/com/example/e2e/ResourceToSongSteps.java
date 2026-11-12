package com.example.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceToSongSteps {

    private static final String RESOURCE_URL = System.getProperty("resource.service.url", "http://localhost:8080");
    private static final String SONG_URL = System.getProperty("song.service.url", "http://localhost:8081");

    private Integer resourceId;

    @When("I upload the mp3 file {string} to resource-service")
    public void uploadMp3(String fileName) throws IOException {
        uploadFile(fileName);
    }

    @Given("a mp3 file {string} is uploaded to resource-service")
    public void givenUploadMp3(String fileName) throws IOException {
        uploadFile(fileName);
    }

    private void uploadFile(String fileName) throws IOException {
        byte[] audio;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("audio/" + fileName)) {
            if (in == null) {
                throw new IllegalStateException("Test file not found: audio/" + fileName);
            }
            audio = in.readAllBytes();
        }

        Response response = given()
                .baseUri(RESOURCE_URL)
                .contentType("audio/mpeg")
                .body(audio)
                .when()
                .post("/resources");

        response.then().statusCode(200);
        resourceId = response.jsonPath().getInt("id");
    }

    @Then("song metadata for that resource appears in song-service within {int} seconds")
    public void songMetadataAppears(int seconds) {
        await().atMost(Duration.ofSeconds(seconds))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> given()
                        .baseUri(SONG_URL)
                        .get("/songs/" + resourceId)
                        .then()
                        .statusCode(200));

        Response song = given().baseUri(SONG_URL).get("/songs/" + resourceId);
        assertFalse(song.jsonPath().getString("name").isBlank());
        assertTrue(song.jsonPath().getString("duration").matches("\\d{2}:[0-5]\\d"));
    }

    @When("I delete the uploaded resource")
    public void deleteResource() {
        given()
                .baseUri(RESOURCE_URL)
                .delete("/resources?id=" + resourceId)
                .then()
                .statusCode(200);
    }

    @Then("song metadata is removed from song-service within {int} seconds")
    public void songMetadataRemoved(int seconds) {
        await().atMost(Duration.ofSeconds(seconds))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> given()
                        .baseUri(SONG_URL)
                        .get("/songs/" + resourceId)
                        .then()
                        .statusCode(404));
    }
}
