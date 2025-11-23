# Trading Application

A comprehensive reactive trading backend application built with Spring Boot 3.5.8, Java 17, Spring WebFlux, and H2 database.

## Features

### Core Trading Features
- **User Management**: User registration with encrypted passwords
- **Account Management**: Multiple accounts per user, balance tracking, deposits/withdrawals
- **Order Management**: Place, modify, and cancel orders (Market, Limit, Stop-Loss)
- **Trade Execution**: Automated matching engine for buy/sell orders
- **Portfolio Management**: Real-time holdings, P&L calculation, position tracking
- **Market Data**: Real-time stock prices with streaming updates
- **Watchlist**: Track favorite stocks

### Technical Features
- **Reactive Programming**: Built with Spring WebFlux and Project Reactor
- **R2DBC**: Reactive database access with H2
- **Server-Sent Events**: Real-time price streaming
- **RESTful API**: Comprehensive REST endpoints
- **H2 Console**: Database inspection at `/h2-console`

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.8
- **Spring WebFlux**: Reactive web framework
- **Spring Data R2DBC**: Reactive database access
- **H2 Database**: In-memory database with file persistence
- **Lombok**: Reduce boilerplate code
- **BCrypt**: Password encryption

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. Navigate to the project directory:
```bash
cd trading-app
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Accessing H2 Console

1. Open browser and navigate to: `http://localhost:8080/h2-console`
2. Use the following connection details:
   - JDBC URL: `jdbc:h2:file:./data/tradingdb`
   - Username: `sa`
   - Password: (leave empty)

## API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username

### Account Management
- `POST /api/accounts` - Create new account
- `GET /api/accounts/{id}` - Get account details
- `GET /api/accounts/user/{userId}` - Get user's accounts
- `POST /api/accounts/{id}/deposit` - Deposit funds
- `POST /api/accounts/{id}/withdraw` - Withdraw funds

### Order Management
- `POST /api/orders` - Place new order
- `GET /api/orders/{id}` - Get order details
- `GET /api/orders/account/{accountId}` - Get account orders
- `PUT /api/orders/{id}/cancel` - Cancel order

### Trade Execution
- `GET /api/trades/{id}` - Get trade details
- `GET /api/trades/stock/{stockId}` - Get trades for stock
- `POST /api/trades/execute` - Execute trade manually
- `POST /api/trades/match/{stockId}` - Run matching engine

### Portfolio
- `GET /api/portfolio/account/{accountId}` - Get holdings
- `GET /api/portfolio/account/{accountId}/details` - Get detailed holdings
- `GET /api/portfolio/account/{accountId}/summary` - Get P&L summary

### Market Data
- `GET /api/market/stocks` - Get all stocks
- `GET /api/market/stocks/{id}` - Get stock by ID
- `GET /api/market/stocks/symbol/{symbol}` - Get stock by symbol
- `GET /api/market/stocks/{stockId}/stream` - Stream real-time prices (SSE)

### Watchlist
- `POST /api/watchlists` - Create watchlist
- `GET /api/watchlists/user/{userId}` - Get user watchlists
- `GET /api/watchlists/{id}/stocks` - Get watchlist stocks
- `POST /api/watchlists/{id}/items` - Add stock to watchlist
- `DELETE /api/watchlists/{watchlistId}/stocks/{stockId}` - Remove stock

## Sample Data

The application comes pre-loaded with:
- 5 sample users
- 10 trading accounts
- 20 stocks across different sectors (Tech, Finance, Healthcare, Energy, Consumer)
- Sample orders, trades, and portfolio holdings
- Watchlists with items

### Sample Users
- Username: `john_trader`, Email: `john@example.com`
- Username: `jane_investor`, Email: `jane@example.com`
- Username: `bob_daytrader`, Email: `bob@example.com`
- Username: `alice_longterm`, Email: `alice@example.com`
- Username: `charlie_swing`, Email: `charlie@example.com`

Password for all users: `password123`

### Sample Stocks
- AAPL, MSFT, GOOGL, NVDA, TSLA (Technology)
- JPM, BAC, GS (Finance)
- JNJ, PFE, UNH (Healthcare)
- XOM, CVX (Energy)
- AMZN, WMT, HD, NKE, SBUX, MCD, DIS (Consumer)

## Example API Calls

### Get All Stocks
```bash
curl http://localhost:8080/api/market/stocks
```

### Place a Market Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "stockId": 1,
    "orderType": "MARKET",
    "side": "BUY",
    "quantity": 10
  }'
```

### Get Portfolio Summary
```bash
curl http://localhost:8080/api/portfolio/account/1/summary
```

### Stream Real-Time Prices
```bash
curl http://localhost:8080/api/market/stocks/1/stream
```

## Project Structure

```
trading-app/
├── src/main/java/com/trading/
│   ├── config/              # Configuration classes
│   ├── controller/          # REST controllers
│   ├── dto/                 # Data transfer objects
│   ├── exception/           # Exception handlers
│   ├── model/               # Domain entities
│   ├── repository/          # R2DBC repositories
│   ├── service/             # Business logic
│   └── TradingApplication.java
├── src/main/resources/
│   ├── application.yml      # Application configuration
│   ├── schema.sql          # Database schema
│   └── data.sql            # Mock data
└── pom.xml                 # Maven dependencies
```

## Database Schema

The application uses the following main tables:
- `users` - User accounts
- `accounts` - Trading accounts
- `stocks` - Tradable instruments
- `orders` - Buy/sell orders
- `trades` - Executed trades
- `portfolio_holdings` - User positions
- `watchlists` - User watchlists
- `watchlist_items` - Stocks in watchlists
- `market_data` - Historical price data

## License

This project is created for demonstration purposes.
