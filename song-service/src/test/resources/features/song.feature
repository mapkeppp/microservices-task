Feature: Song metadata management

  Scenario: Creating a new song returns 201
    When I create a song with the following details
      | id | name              | artist | album                | duration | year |
      | 1  | Bohemian Rhapsody | Queen  | A Night at the Opera | 05:55    | 1975 |
    Then the response status is 201
    And the response contains the song id 1

  Scenario: Creating a song with an existing id returns 409
    Given a song with id 1 exists
    When I create a song with the following details
      | id | name              | artist | album                | duration | year |
      | 1  | Bohemian Rhapsody | Queen  | A Night at the Opera | 05:55    | 1975 |
    Then the response status is 409

  Scenario: Creating a song with an invalid duration returns 400
    When I create a song with the following details
      | id | name              | artist | album                | duration | year |
      | 2  | Bohemian Rhapsody | Queen  | A Night at the Opera | 5:55     | 1975 |
    Then the response status is 400

  Scenario: Retrieving an existing song returns 200
    Given a song with id 1 exists
    When I request song 1
    Then the response status is 200
    And the response has name "Bohemian Rhapsody"

  Scenario: Retrieving a missing song returns 404
    When I request song 999
    Then the response status is 404

  Scenario: Updating an existing song returns 200
    Given a song with id 1 exists
    When I update song 1 with the following details
      | id | name                          | artist | album                | duration | year |
      | 1  | Bohemian Rhapsody (Remastered) | Queen  | A Night at the Opera | 05:55    | 1975 |
    Then the response status is 200
    And the response has name "Bohemian Rhapsody (Remastered)"

  Scenario: Updating a missing song returns 404
    When I update song 999 with the following details
      | id  | name              | artist | album                | duration | year |
      | 999 | Bohemian Rhapsody | Queen  | A Night at the Opera | 05:55    | 1975 |
    Then the response status is 404

  Scenario: Deleting songs returns 204 and removes them
    Given a song with id 1 exists
    And a song with id 2 exists
    When I delete songs "1,2"
    Then the response status is 204
    And the song with id 1 does not exist
    And the song with id 2 does not exist