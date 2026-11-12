Feature: Upload an mp3 and get its metadata through all services

  Scenario: Uploaded mp3 is processed and its metadata is stored in song-service
    When I upload the mp3 file "downloaded.mp3" to resource-service
    Then song metadata for that resource appears in song-service within 30 seconds

  Scenario: Deleting a resource removes its metadata from song-service
    Given a mp3 file "downloaded.mp3" is uploaded to resource-service
    And song metadata for that resource appears in song-service within 30 seconds
    When I delete the uploaded resource
    Then song metadata is removed from song-service within 30 seconds
