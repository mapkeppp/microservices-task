package com.example.song_service.cucumber;

import com.example.song_service.entity.Song;
import com.example.song_service.repository.SongRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SongSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions lastResult;

    @Before
    public void cleanDatabase() {
        songRepository.deleteAll();
    }

    @Given("a song with id {int} exists")
    public void aSongWithIdExists(int id) {
        Song song = new Song();
        song.setId(id);
        song.setName("Bohemian Rhapsody");
        song.setArtist("Queen");
        song.setAlbum("A Night at the Opera");
        song.setDuration("05:55");
        song.setYear("1975");
        songRepository.save(song);
    }

    @When("I create a song with the following details")
    public void iCreateASong(DataTable table) throws Exception {
        lastResult = mockMvc.perform(post("/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(table)));
    }

    @When("I update song {int} with the following details")
    public void iUpdateASong(int id, DataTable table) throws Exception {
        lastResult = mockMvc.perform(put("/songs/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(table)));
    }

    @When("I request song {int}")
    public void iRequestASong(int id) throws Exception {
        lastResult = mockMvc.perform(get("/songs/{id}", id));
    }

    @When("I delete songs {string}")
    public void iDeleteSongs(String ids) throws Exception {
        lastResult = mockMvc.perform(delete("/songs").param("id", ids));
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) throws Exception {
        lastResult.andExpect(status().is(status));
    }

    @Then("the response contains the song id {int}")
    public void theResponseContainsId(int id) throws Exception {
        lastResult.andExpect(jsonPath("$.id").value(id));
    }

    @Then("the response has name {string}")
    public void theResponseHasName(String name) throws Exception {
        lastResult.andExpect(jsonPath("$.name").value(name));
    }

    @Then("the song with id {int} exists")
    public void theSongExists(int id) {
        assertTrue(songRepository.existsById(id));
    }

    @Then("the song with id {int} does not exist")
    public void theSongDoesNotExist(int id) {
        assertFalse(songRepository.existsById(id));
    }

    private String toJson(DataTable table) throws Exception {
        Map<String, String> row = table.asMaps().get(0);
        Map<String, Object> body = new HashMap<>();
        body.put("id", Integer.parseInt(row.get("id")));
        body.put("name", row.get("name"));
        body.put("artist", row.get("artist"));
        body.put("album", row.get("album"));
        body.put("duration", row.get("duration"));
        body.put("year", row.get("year"));
        return objectMapper.writeValueAsString(body);
    }
}