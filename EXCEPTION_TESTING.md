# Trading Exception Testing Guide

This guide describes how to test exception handling in the trading application using real business logic scenarios.

## Overview

The `TradingExceptionTestController` provides endpoints to test actual exception flows in the trading application's business logic, including:
- User management errors
- Account operation failures
- Order validation errors
- Trade execution issues
- Portfolio management exceptions
- Watchlist operation errors

## Base URL
All trading exception test endpoints are under: `/api/test/trading-exceptions`

---

## Available Test Endpoints

### 1. Health Check
**Endpoint:** `GET /api/test/trading-exceptions/health`

**Description:** Verify the controller is working

**Expected Response:** 200 OK
```json
{
  "status": "OK",
  "message": "Trading exception test controller is ready"
}
```

---

### 2. List All Test Endpoints
**Endpoint:** `GET /api/test/trading-exceptions/list`

**Description:** Get a list of all available exception test endpoints

**Expected Response:** 200 OK with endpoint descriptions

---

## User Management Exception Tests

### 3. Duplicate User Registration
**Endpoint:** `GET /api/test/trading-exceptions/duplicate-user`

**Scenario:** Attempt to register a user with an existing username

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username or email already exists"
}
```

**Business Logic Tested:** `UserService.registerUser()` validation

---

## Account Management Exception Tests

### 4. Insufficient Balance Withdrawal
**Endpoint:** `GET /api/test/trading-exceptions/insufficient-balance`

**Scenario:** Attempt to withdraw more money than available in account

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance"
}
```

**Business Logic Tested:** `AccountService.withdraw()` balance validation

---

### 5. Account Not Found
**Endpoint:** `GET /api/test/trading-exceptions/account-not-found`

**Scenario:** Attempt to access a non-existent account

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Account with ID 99999 not found"
}
```

**Business Logic Tested:** `AccountService.findById()` existence check

---

### 6. Inactive Account Operation
**Endpoint:** `GET /api/test/trading-exceptions/inactive-account`

**Scenario:** Attempt to perform operations on an inactive account

**Expected Exception:** `IllegalStateException`

**Expected Response:** 409 Conflict
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 409,
  "error": "Conflict",
  "message": "Account is not active or does not exist"
}
```

**Business Logic Tested:** `AccountService` account status validation

---

## Order Management Exception Tests

### 7. Order with Insufficient Balance
**Endpoint:** `GET /api/test/trading-exceptions/order-insufficient-balance`

**Scenario:** Place a buy order that exceeds account balance

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance"
}
```

**Business Logic Tested:** `OrderService.placeOrder()` balance validation for buy orders

---

### 8. Order with Invalid Stock
**Endpoint:** `GET /api/test/trading-exceptions/order-invalid-stock`

**Scenario:** Place an order for a non-existent stock

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock not found"
}
```

**Business Logic Tested:** `OrderService.placeOrder()` stock existence validation

---

### 9. Order with Invalid Account
**Endpoint:** `GET /api/test/trading-exceptions/order-invalid-account`

**Scenario:** Place an order for a non-existent account

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Account not found"
}
```

**Business Logic Tested:** `OrderService.placeOrder()` account existence validation

---

### 10. LIMIT Order Without Price
**Endpoint:** `GET /api/test/trading-exceptions/limit-order-no-price`

**Scenario:** Place a LIMIT order without specifying a price

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Price is required for LIMIT and STOP_LOSS orders"
}
```

**Business Logic Tested:** `OrderService.placeOrder()` order type validation

---

### 11. Cancel Executed Order
**Endpoint:** `GET /api/test/trading-exceptions/cancel-executed-order`

**Scenario:** Attempt to cancel an already executed order

**Expected Exception:** `IllegalStateException`

**Expected Response:** 409 Conflict
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot cancel executed order"
}
```

**Business Logic Tested:** `OrderService.cancelOrder()` order status validation

---

### 12. Cancel Already Cancelled Order
**Endpoint:** `GET /api/test/trading-exceptions/cancel-cancelled-order`

**Scenario:** Attempt to cancel an already cancelled order

**Expected Exception:** `IllegalStateException`

**Expected Response:** 409 Conflict
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 409,
  "error": "Conflict",
  "message": "Order already cancelled"
}
```

**Business Logic Tested:** `OrderService.cancelOrder()` duplicate cancellation prevention

---

## Portfolio Management Exception Tests

### 13. Sell with Insufficient Holdings
**Endpoint:** `GET /api/test/trading-exceptions/sell-insufficient-holdings`

**Scenario:** Attempt to sell more shares than owned

**Expected Exception:** `IllegalStateException`

**Expected Response:** 409 Conflict
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient holdings to sell"
}
```

**Business Logic Tested:** `PortfolioService.updateHoldingAfterSell()` holdings validation

---

## Watchlist Exception Tests

### 14. Add Invalid Stock to Watchlist
**Endpoint:** `GET /api/test/trading-exceptions/watchlist-invalid-stock`

**Scenario:** Add a non-existent stock to a watchlist

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock not found"
}
```

**Business Logic Tested:** `WatchlistService.addStockToWatchlist()` stock validation

---

### 15. Add Stock to Invalid Watchlist
**Endpoint:** `GET /api/test/trading-exceptions/invalid-watchlist`

**Scenario:** Add a stock to a non-existent watchlist

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Watchlist not found"
}
```

**Business Logic Tested:** `WatchlistService.addStockToWatchlist()` watchlist validation

---

## Trade Execution Exception Tests

### 16. Trade with Mismatched Stocks
**Endpoint:** `GET /api/test/trading-exceptions/trade-mismatched-stocks`

**Scenario:** Execute trade between orders for different stocks

**Expected Exception:** `IllegalArgumentException`

**Expected Response:** 400 Bad Request
```json
{
  "timestamp": "2025-11-23T14:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Orders are for different stocks"
}
```

**Business Logic Tested:** `TradeService.executeTrade()` stock matching validation

---

## Exception Mapping Summary

| Service | Validation | Exception Type | HTTP Status |
|---------|-----------|----------------|-------------|
| UserService | Duplicate username/email | IllegalArgumentException | 400 |
| AccountService | Insufficient balance | IllegalArgumentException | 400 |
| AccountService | Account not found | IllegalArgumentException | 400 |
| AccountService | Inactive account | IllegalStateException | 409 |
| OrderService | Insufficient balance | IllegalArgumentException | 400 |
| OrderService | Invalid stock | IllegalArgumentException | 400 |
| OrderService | Invalid account | IllegalArgumentException | 400 |
| OrderService | Missing price for LIMIT | IllegalArgumentException | 400 |
| OrderService | Cancel executed order | IllegalStateException | 409 |
| OrderService | Cancel cancelled order | IllegalStateException | 409 |
| PortfolioService | Insufficient holdings | IllegalStateException | 409 |
| WatchlistService | Invalid stock | IllegalArgumentException | 400 |
| WatchlistService | Invalid watchlist | IllegalArgumentException | 400 |
| TradeService | Mismatched stocks | IllegalArgumentException | 400 |

---

## Testing Examples

### Using curl

```bash
# Test duplicate user registration
curl http://localhost:8080/api/test/trading-exceptions/duplicate-user

# Test insufficient balance
curl http://localhost:8080/api/test/trading-exceptions/insufficient-balance

# Test order with insufficient balance
curl http://localhost:8080/api/test/trading-exceptions/order-insufficient-balance

# Test invalid stock order
curl http://localhost:8080/api/test/trading-exceptions/order-invalid-stock

# Test cancel executed order
curl http://localhost:8080/api/test/trading-exceptions/cancel-executed-order

# Test sell with insufficient holdings
curl http://localhost:8080/api/test/trading-exceptions/sell-insufficient-holdings

# Get all test endpoints
curl http://localhost:8080/api/test/trading-exceptions/list
```

### Using PowerShell

```powershell
# Test duplicate user
Invoke-WebRequest http://localhost:8080/api/test/trading-exceptions/duplicate-user

# Test insufficient balance
Invoke-WebRequest http://localhost:8080/api/test/trading-exceptions/insufficient-balance

# Test order validations
Invoke-WebRequest http://localhost:8080/api/test/trading-exceptions/order-invalid-account
Invoke-WebRequest http://localhost:8080/api/test/trading-exceptions/limit-order-no-price
```

---

## Benefits of This Approach

1. **Real Business Logic Testing**: Tests actual service layer validation, not dummy exceptions
2. **Comprehensive Coverage**: Covers all major business flows (users, accounts, orders, trades, portfolio, watchlist)
3. **Production-Ready**: Tests the same validation logic that runs in production
4. **Easy Verification**: Simple GET endpoints make it easy to test exception handling
5. **Documentation**: Each endpoint documents a specific business rule and validation

---

## Notes

- All tests use existing mock data from the database
- Tests are designed to trigger specific validation failures
- The GlobalExceptionHandler catches and formats all exceptions consistently
- These endpoints should be secured or disabled in production environments
- Each test demonstrates a real-world error scenario that could occur in the trading application
