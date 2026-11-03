Feature: Song Service API

  Scenario: Successfully create a new song
    Given I have a song with title "Bohemian Rhapsody" and artist "Queen"
    When I send a POST request to create the song
    Then the response status should be 201
    And the response should contain the song ID

  Scenario: Fail to create a song with duplicate title
    Given a song with title "Stairway to Heaven" already exists
    When I try to create another song with title "Stairway to Heaven"
    Then the response status should be 409

  Scenario: Retrieve a song by ID
    Given I have a song with title "Hotel California" and artist "Eagles"
    When I send a POST request to create the song
    And I retrieve the created song
    Then the response status should be 200
    And the song title should be "Hotel California"

  Scenario: Delete a song
    Given I have a song with title "Comfortably Numb" and artist "Pink Floyd"
    When I send a POST request to create the song
    And I delete the created song
    Then the response status should be 204
    And the song should not exist

  Scenario: Update a song
    Given I have a song with title "Original Title" and artist "Artist"
    When I send a POST request to create the song
    And I update the song with title "New Title"
    Then the response status should be 200
    And the song title should be "New Title"