Feature: Video Download

  Scenario: Generate presigned URL when ZIP exists
    Given a video with id "550e8400-e29b-41d4-a716-446655440000" belonging to user "user-123"
    And the ZIP file exists in storage
    When a download URL request is made for videoId "550e8400-e29b-41d4-a716-446655440000" and userId "user-123"
    Then the response should contain a presigned URL
    And the response status should be 200

  Scenario: Return 404 when ZIP does not exist
    Given a video with id "660e8400-e29b-41d4-a716-446655440000" belonging to user "user-456"
    And the ZIP file does not exist in storage
    When a download URL request is made for videoId "660e8400-e29b-41d4-a716-446655440000" and userId "user-456"
    Then the response status should be 404

  Scenario: Return 404 for wrong userId (ownership check)
    Given a video with id "770e8400-e29b-41d4-a716-446655440000" belonging to user "user-789"
    And only the ZIP for user "user-789" exists
    When a download URL request is made for videoId "770e8400-e29b-41d4-a716-446655440000" and userId "user-999"
    Then the response status should be 404

  Scenario: Login without AUTH_LAMBDA_URL returns 503
    When a login request is made without AUTH_LAMBDA_URL configured
    Then the response status should be 503
