@customers
Feature: Customer Favorites Management
  As an authenticated customer
  I want to manage my product favorites list
  So that I can quickly access products I like

  Background:
    Given an authenticated user with standard QA credentials

  @smoke @positive
  Scenario: Retrieve customer favorites list
    When the user retrieves their favorites
    Then the response status code is 200

  @positive
  Scenario: Add a product to favorites
    When the user adds product "SKU-TEST-001" to favorites
    Then the response status code is 200
    # @After: CleanupRegistry removes SKU-TEST-001 from favorites automatically

  @positive
  Scenario: Add and then manually remove a product from favorites
    When the user adds product "SKU-TEST-002" to favorites
    Then the response status code is 200
    When the user removes product "SKU-TEST-002" from favorites
    Then the response status code is 200
    # No cleanup needed — already removed manually
    # CleanupRegistry still has the registered action, but a failed DELETE is
    # swallowed with a warning (idempotent cleanup)

  @negative
  Scenario: Unauthenticated request to favorites returns 401
    When I send a GET request to "/api/v1/customers/30000001/favorites"
    Then the response status code is 401
    And the response matches the error schema with status 401
