@auth
Feature: Login with OTP Code
  As a Farmacity SuperApp user
  I want to authenticate using a one-time code sent to my email
  So that I can log in without a password

  @smoke @positive
  Scenario: OTP send request is accepted for a registered email
    When the user requests an OTP for email "test404Vic@yopmail.com"
    Then the response status code is 200
    And the response field "status" equals "success"

  @negative
  Scenario: OTP send fails for invalid email format
    When the user requests an OTP for email "invalid-email"
    Then the response status code is 400
    And the response matches the error schema with status 400

  @negative
  Scenario: OTP validation fails with wrong code
    When the user validates OTP code "000000" for email "qa-auto@farmacity-test.com"
    Then the response status code is 401
    And the response matches the error schema with status 401

  @smoke @positive @wip
  #TODO:  Revisar estrategia para no bloquear un user real con intentos fallidos de OTP validation.
  # Posible solución: usar un email de prueba dedicado para este escenario, o mockear la validación de OTP.
  Scenario: Password recovery request accepted for registered email
    When the user initiates password recovery for email "test404Vic@yopmail.com"
    Then the response status code is 200
    And the response field "status" equals "success"

  @negative
  Scenario: Password recovery fails for invalid email format
    When the user initiates password recovery for email "bad-email"
    Then the response status code is 400
    And the response matches the error schema with status 400
