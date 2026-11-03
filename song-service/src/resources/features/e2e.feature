Feature: End-to-End Song Service

  Scenario: Complete song lifecycle
    Given the service is running
    When I create a song "Hotel California" by "Eagles"
    Then the song is created successfully
    When I retrieve the song
    Then the song details are correct
    When I update the song duration to 391
    Then the duration is updated
    When I delete the song
    Then the song is deleted

  Scenario: Multiple songs management
    Given the service is running
    When I create 3 different songs
    Then I have 3 songs
    When I delete one song
    Then I have 2 songs

  Scenario: Search and retrieve songs
    Given the service is running
    When I create multiple songs with different artists
    Then I can retrieve all songs