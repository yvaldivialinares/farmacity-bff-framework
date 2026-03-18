@auth
Feature: Login with Email and Password
  As a Farmacity SuperApp user
  I want to authenticate using my email and password
  So that I can access protected features of the app

  @smoke @positive
  Scenario: Successful login with valid credentials returns token
    When the user logs in with email "test404Vic@yopmail.com" and password "Farmacity2"
    Then the response status code is 200
    And the login response contains a valid token

  @smoke @positive
  Scenario: Token obtained from login can be used for authenticated requests
    When the user logs in with email "test404Vic@yopmail.com" and password "Farmacity2"
    Then the response status code is 200
    And the token is stored for subsequent requests
    When I send a GET request to "/api/v1.0/healthinsurances"
    Then the response status code is 200

  @negative
  Scenario: Login fails with incorrect password
    When the user logs in with email "qa-auto@farmacity-test.com" and password "WrongPassword!"
    Then the response status code is 401
    And the response matches the error schema with status 401

  @negative
  Scenario: Login fails with invalid email format
    When the user logs in with email "not-an-email" and password "anyPassword123"
    Then the response status code is 400
    And the response matches the error schema with status 400

  @negative
  Scenario: Login fails with empty password
    When the user logs in with email "qa-auto@farmacity-test.com" and password ""
    Then the response status code is 400
    And the response matches the error schema with status 400

  @negative
  Scenario: Login fails with empty email
    When the user logs in with email "" and password "anyPassword123"
    Then the response status code is 400
    And the response matches the error schema with status 400

  @negative
  Scenario Outline: Login fails with various invalid credential combinations
    When the user logs in with email "<email>" and password "<password>"
    Then the response status code is <status>
    And the response matches the error schema with status <status>
    Examples:
      | email                        | password        | status |
      | nonexistent@farmacity-test.com | ValidPass@123 | 401    |
      | qa-auto@farmacity-test.com   | wrong           | 401    |
      | bad-format                   | ValidPass@123   | 400    |
