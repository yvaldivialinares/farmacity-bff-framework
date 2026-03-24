@shopping
Feature: Shopping Cart and Checkout Flow
  As an authenticated customer
  I want to build a cart and complete a purchase
  So that I can receive products from Farmacity

  Background:
    Given an authenticated user with standard QA credentials

  @smoke @positive
  Scenario: Create a new shopping cart
    Given the user has shopping cart creation data
    When the user creates a new shopping cart with prepared data
    Then the cart is created successfully

  @positive
  Scenario: Add an item to the cart and verify it is present
    When the user creates a new shopping cart
    Then the cart is created successfully
    When the user adds product with SKU "12345" and quantity 1 to the cart
    Then the item is added to the cart
    When the user retrieves the cart
    Then the response status code is 200
    And the response field "id" is not null

  @positive
  Scenario: Apply a coupon to an existing cart
    When the user creates a new shopping cart
    Then the cart is created successfully
    When the user adds product with SKU "12345" and quantity 2 to the cart
    Then the item is added to the cart
    When the user applies coupon "DESCUENTO10"
    Then the response status code is 200
    # @After: CleanupRegistry removes coupon if checkout didn't happen

  @negative
  Scenario: Cart item update with zero quantity should fail
    When the user creates a new shopping cart
    Then the cart is created successfully
    When the user adds product with SKU "12345" and quantity 1 to the cart
    Then the item is added to the cart
    When the user updates the cart item quantity to 0
    Then the response status code is 400
    And the response matches the error schema with status 400

  @flow @wip
  Scenario: Complete end-to-end purchase flow
  # Tagged @wip until checkout integration is confirmed stable in QA env.
  # Remove @wip to activate.
    When the user creates a new shopping cart
    Then the cart is created successfully
    When the user adds product with SKU "12345" and quantity 1 to the cart
    Then the item is added to the cart
    When the user confirms checkout
    Then the order is created successfully
    When the user retrieves the order details
    Then the response status code is 200
    And the order status is "payment-pending"
    When the user retrieves their order history
    Then the new order appears in the history
