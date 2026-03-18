@customers
Feature: Customer Address Management
  As an authenticated customer
  I want to manage my delivery addresses
  So that I can receive orders at my preferred locations

  # Background authenticates once and verifies the test customer profile exists.
  # Every scenario in this file starts from this clean, authenticated state.
  Background:
    Given an authenticated user with standard QA credentials
    And the customer profile exists for the authenticated user

  @smoke @positive
  Scenario: Retrieve customer address list
    When the user retrieves their addresses
    Then the response status code is 200

  @positive
  Scenario: Add a new address and verify it appears in the address list
    When the user adds a new address
    Then the address is created successfully
    # ↑ CleanupRegistry now holds: DELETE /customers/{dni}/addresses/{id}
    When the user retrieves their addresses
    Then the address list contains the created address
    # @After hook: CleanupRegistry runs → address is deleted automatically

  @positive
  Scenario: Add two addresses sequentially — both cleaned up in LIFO order
    When the user adds a new address
    Then the address is created successfully
    # ↑ Cleanup #1 registered: delete address A
    When the user adds a new address
    Then the address is created successfully
    # ↑ Cleanup #2 registered: delete address B
    # @After: deletes B first, then A (LIFO order — safe for any dependency)

  @positive
  Scenario: Add an address and set it as default
    When the user adds a new address
    Then the address is created successfully
    When the user sets the created address as default
    Then the response status code is 200
    # @After: cleanup removes the default address

  @negative
  Scenario: Retrieve addresses for a non-existent customer returns 404
    When I send a GET request to "/api/v1/customers/00000000/addresses"
    Then the response status code is 404
    And the response matches the error schema with status 404

  @negative
  Scenario: Unauthenticated request to address list returns 401
    When I send a GET request to "/api/v1/customers/30000001/addresses"
    Then the response status code is 401
    And the response matches the error schema with status 401
