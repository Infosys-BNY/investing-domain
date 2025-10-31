# Product Requirements Document: Holdings Data View Screen

## 1. Executive Summary

### Product Overview
The Holdings Data View Screen provides BNY advisors with a comprehensive view of a selected client's security holdings, including detailed position information, cost basis, market values, and performance metrics. This screen is accessed after selecting a client from the Client Selection Screen.

### Business Objective
Enable BNY advisors to analyze client portfolio holdings efficiently, identify opportunities for optimization, and make informed recommendations for portfolio adjustments, including tax loss harvesting opportunities.

### Target Users
- Primary: BNY Financial Advisors actively managing client portfolios
- Secondary: Portfolio Analysts, Wealth Managers, Tax Advisors

## 2. User Stories

### Primary User Stories
1. **As a BNY advisor**, I want to see all holdings for my selected client in a clear table format, so that I can quickly assess the portfolio composition.
2. **As a BNY advisor**, I want to view unrealized gains and losses for each position, so that I can identify tax loss harvesting opportunities.
3. **As a BNY advisor**, I want to sort and filter holdings by various criteria, so that I can focus on specific securities or sectors.
4. **As a BNY advisor**, I want to export holdings data, so that I can perform offline analysis or share with clients.
5. **As a BNY advisor**, I want to see real-time or near-real-time pricing, so that I can make timely recommendations.

## 3. Functional Requirements

### 3.1 Header Information
- **Client Context Bar**:
  - Client Name
  - Account Number
  - Account Type
  - Total Portfolio Value
  - Total Cash Position
  - As of Date/Time (data freshness indicator)
  - "Back to Client List" navigation

### 3.2 Portfolio Summary Section
- **Key Metrics Dashboard**:
  - Total Market Value
  - Total Cost Basis
  - Total Unrealized Gain/Loss ($ and %)
  - Total Realized Gain/Loss (YTD)
  - Number of Holdings
  - Portfolio Beta
  - Dividend Yield

### 3.3 Holdings Table

#### 3.3.1 Table Columns
| Column | Description | Format | Sortable |
|--------|-------------|--------|----------|
| Symbol/Ticker | Security identifier | Text | Yes |
| Security Name | Full security description | Text | Yes |
| Quantity | Number of shares/units | Number (comma separated) | Yes |
| Price | Current market price | Currency ($X.XX) | Yes |
| Cost Basis | Average cost per share | Currency ($X.XX) | Yes |
| Total Cost | Quantity × Cost Basis | Currency | Yes |
| Market Value | Quantity × Current Price | Currency | Yes |
| Unrealized G/L | Market Value - Total Cost | Currency (color coded) | Yes |
| % Gain/Loss | (Unrealized G/L / Total Cost) × 100 | Percentage (color coded) | Yes |
| % of Portfolio | (Market Value / Total Portfolio Value) × 100 | Percentage | Yes |
| Sector | Security sector classification | Text | Yes |
| Asset Class | Equity/Fixed Income/Alternative | Text | Yes |
| Tax Lot | Expandable to show individual lots | Link/Expandable | No |

#### 3.3.2 Visual Indicators
- **Color Coding**:
  - Green for positive gains (>0%)
  - Red for losses (<0%)
  - Gray for flat positions (0%)
- **Icons**:
  - 📈 for top gainers (top 10%)
  - 📉 for top losers (bottom 10%)
  - ⚠️ for concentrated positions (>10% of portfolio)
  - 🔔 for positions with alerts

### 3.4 Filtering and Search

#### 3.4.1 Quick Filters (Toggle Buttons)
- Show All
- Gains Only
- Losses Only
- Equities
- Fixed Income
- Alternatives
- Large Positions (>5% of portfolio)

#### 3.4.2 Advanced Filters (Dropdown Panel)
- Gain/Loss Range (slider or input)
- Market Value Range
- Sector Selection (multi-select)
- Holding Period (Short-term/Long-term)
- Dividend Paying (Yes/No)
- ESG Rated (Yes/No)

#### 3.4.3 Search Functionality
- Search by symbol or security name
- Auto-complete after 2 characters
- Highlight matching results

### 3.5 Actions and Tools

#### 3.5.1 Row-Level Actions
- View Security Details (opens modal/sidebar)
- View Tax Lots
- Trade History
- Add to Watchlist
- Create Trade Order (redirects to trading platform)

#### 3.5.2 Bulk Actions
- Select multiple holdings via checkboxes
- Export Selected
- Create Rebalancing Proposal
- Generate Tax Loss Report

#### 3.5.3 Export Options
- Export to Excel (.xlsx)
- Export to CSV
- Export to PDF (formatted report)
- Email Report to Client (with approval workflow)

### 3.6 Tax Lot Expansion View
When "View Tax Lots" is clicked:
- Expand row to show individual tax lots
- Display for each lot:
  - Purchase Date
  - Quantity
  - Cost Basis
  - Current Value
  - Gain/Loss
  - Holding Period (ST/LT indicator)
  - Tax Impact Estimate

### 3.7 Real-Time Updates
- Price updates every 15 seconds during market hours
- Visual indicator for price changes (up/down arrows)
- "Last Updated" timestamp
- Manual refresh button
- Auto-pause updates when user is interacting with data

## 4. Non-Functional Requirements

### 4.1 Performance
- Initial data load: < 3 seconds for up to 500 holdings
- Sort operations: < 200ms
- Filter applications: < 500ms
- Export generation: < 5 seconds for 1000 holdings
- Real-time price updates: < 1 second latency

### 4.2 Data Accuracy
- Prices: 15-minute delayed for non-premium users, real-time for premium
- Cost basis: Reconciled daily with custodian
- Corporate actions: Updated within 24 hours
- Tax lots: Matched with custodian records

### 4.3 Security
- Data encryption at rest and in transit
- Column-level data masking for sensitive information
- Audit trail for all data exports
- Session-based access with automatic timeout
- Read-only access (no direct editing of holdings)

### 4.4 Accessibility
- WCAG 2.1 AA compliance
- Keyboard navigation for all functions
- Screen reader compatible table structure
- High contrast mode
- Adjustable font sizes

### 4.5 Browser Compatibility
- Chrome (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Edge (latest 2 versions)

## 5. User Interface Design

### 5.1 Layout Structure
```
+----------------------------------------+
| ← Back to Clients | John Smith - XXX1234|
| Total Value: $1.2M | As of: 3:45 PM ET |
+----------------------------------------+
| Summary Metrics                        |
| [MV: $1.2M] [G/L: +$45K] [Yield: 2.3%] |
+----------------------------------------+
| [Search...] [Filters ▼] [Export] [⟳]   |
+----------------------------------------+
| ☐ | Symbol | Name | Qty | Price | Value| G/L |
|---|--------|------|-----|-------|------|-----|
| ☐ | AAPL | Apple | 100 | $150 | $15K | +20%|
|   |      |       |     |      |      | 🟢  |
|---|--------|------|-----|-------|------|-----|
| ☐ | MSFT | Microsoft | 50 | $300 | $15K | -5%|
|   |      |          |    |      |      | 🔴  |
|---|--------|------|-----|-------|------|-----|
| [Show 50 ▼] Page 1 of 3 [< 1 2 3 >]    |
+----------------------------------------+
```

### 5.2 Visual Design Specifications

#### 5.2.1 Color Palette
- Primary: BNY Blue (#003087)
- Success/Gains: Green (#28A745)
- Warning/Losses: Red (#DC3545)
- Neutral: Gray (#6C757D)
- Background: Light Gray (#F8F9FA)
- Table Alternating Rows: #FFFFFF / #F2F2F2

#### 5.2.2 Typography
- Headers: Roboto Medium 16px
- Table Headers: Roboto Medium 14px
- Table Data: Roboto Regular 13px
- Metrics: Roboto Bold 18px

#### 5.2.3 Spacing and Alignment
- Table cell padding: 12px
- Row height: 48px
- Margin between sections: 24px
- Right-align numerical columns
- Left-align text columns

### 5.3 Interactive Elements

#### 5.3.1 Hover States
- Row highlight on hover (#E9ECEF)
- Tooltip on abbreviated values
- Button color darkening
- Cursor changes for clickable elements

#### 5.3.2 Loading States
- Skeleton screens during initial load
- Inline spinners for updates
- Progress bar for exports
- Shimmer effect for real-time updates

#### 5.3.3 Empty States
- "No holdings found" message
- Suggested actions (e.g., "Check filters" or "Contact support")
- Illustration or icon

## 6. Data Requirements

### 6.1 Data Sources
- Market Data Provider (real-time prices)
- Custodian System (positions and cost basis)
- Reference Data System (security master)
- Corporate Actions System
- Tax Lot Accounting System

### 6.2 Data Refresh Schedule
| Data Type | Refresh Frequency | Source |
|-----------|------------------|--------|
| Prices | Real-time/15-min delay | Market Data Provider |
| Positions | Real-time | Custodian |
| Cost Basis | Daily reconciliation | Custodian |
| Corporate Actions | Intraday | Corp Actions System |
| Tax Lots | On-demand | Tax System |

### 6.3 Data Calculations
- **Unrealized Gain/Loss**: Market Value - Cost Basis
- **% Gain/Loss**: ((Market Value - Cost Basis) / Cost Basis) × 100
- **% of Portfolio**: (Position Market Value / Total Portfolio Value) × 100
- **Weighted Average Cost**: Sum(Lot Cost × Lot Quantity) / Total Quantity

## 7. API Requirements

### 7.1 Primary Endpoints
```
GET /api/v1/accounts/{accountId}/holdings
Response: List of holdings with all required fields

GET /api/v1/securities/{symbol}/price
Response: Current price and price change data

GET /api/v1/accounts/{accountId}/holdings/{symbol}/taxlots
Response: Individual tax lots for a position

POST /api/v1/accounts/{accountId}/holdings/export
Request: Format type and selected holdings
Response: File download link
```

### 7.2 WebSocket Connections
```
ws://api.bny.com/v1/prices/stream
Subscribe to real-time price updates for displayed securities
```

### 7.3 Response Format Example
```json
{
  "accountId": "ACC-789012",
  "asOfDate": "2024-10-31T15:45:00Z",
  "summary": {
    "totalMarketValue": 1200000.00,
    "totalCostBasis": 1155000.00,
    "totalUnrealizedGainLoss": 45000.00,
    "totalRealizedGainLossYTD": 12000.00
  },
  "holdings": [
    {
      "symbol": "AAPL",
      "securityName": "Apple Inc.",
      "quantity": 100,
      "price": 150.00,
      "priceChange": 2.50,
      "priceChangePercent": 1.69,
      "costBasis": 125.00,
      "totalCost": 12500.00,
      "marketValue": 15000.00,
      "unrealizedGainLoss": 2500.00,
      "unrealizedGainLossPercent": 20.00,
      "portfolioPercent": 1.25,
      "sector": "Technology",
      "assetClass": "Equity",
      "hasAlerts": false,
      "taxLotCount": 3
    }
  ],
  "pagination": {
    "totalRecords": 150,
    "currentPage": 1,
    "pageSize": 50,
    "totalPages": 3
  }
}
```

## 8. Error Handling

### 8.1 Error Scenarios
- Failed to load holdings data
- Price feed disconnection
- Export generation failure
- Session timeout
- Insufficient permissions
- No holdings in account

### 8.2 Error Recovery
- Automatic retry for transient failures (3 attempts)
- Fallback to cached data when available
- Manual refresh option
- Clear error messages with resolution steps

## 9. Success Metrics

### 9.1 Performance KPIs
- Page load time: < 3 seconds for 95% of requests
- Data accuracy: 99.9% match with custodian
- Export success rate: > 99%
- Real-time update latency: < 1 second

### 9.2 User Engagement Metrics
- Average time on screen: 3-5 minutes
- Features used per session
- Export frequency
- Filter usage patterns
- Tax lot view engagement

### 9.3 Business Metrics
- Tax loss harvesting opportunities identified
- Portfolio rebalancing actions initiated
- Client reports generated
- Trade orders created from holdings view

## 10. Compliance and Regulatory

### 10.1 Requirements
- FINRA Rule 2111 (Suitability)
- SEC Rule 17a-4 (Record Keeping)
- Best Execution obligations
- Privacy and data protection (GLBA)

### 10.2 Audit Requirements
- Log all data access and exports
- Maintain audit trail for 7 years
- Track user actions and timestamps
- Record data lineage and sources

## 11. Future Enhancements

### Phase 2 Features
- Portfolio analytics and risk metrics
- What-if scenarios and modeling
- Automated tax loss harvesting suggestions
- Integration with trading platform
- Client portal view
- Mobile responsive design
- Comparative analysis tools
- ESG scoring and impact metrics
- Alternative investment details
- Options and derivatives display

### Phase 3 Features
- AI-powered insights and recommendations
- Predictive analytics for gains/losses
- Automated rebalancing proposals
- Integration with financial planning tools
- Real-time collaboration features
- Voice-activated commands

## 12. Dependencies

### 12.1 Technical Dependencies
- Market Data Feed Service
- Custodian Integration API
- Authentication/Authorization Service
- Export Generation Service
- Notification Service

### 12.2 Data Dependencies
- Accurate cost basis from custodian
- Timely corporate action processing
- Complete tax lot information
- Current security reference data

## 13. Acceptance Criteria

1. Holdings load within 3 seconds for accounts with up to 500 positions
2. All columns sort correctly in ascending and descending order
3. Filters accurately reduce the displayed holdings
4. Unrealized gains/losses calculate correctly
5. Export functions generate accurate files in all formats
6. Tax lot expansion shows complete lot details
7. Real-time prices update during market hours
8. Color coding correctly represents gains and losses
9. Navigation back to client list maintains context
10. All accessibility requirements are met

## 14. Appendix

### A. Glossary
- **Cost Basis**: Original purchase price plus adjustments
- **Market Value**: Current price × quantity
- **Unrealized Gain/Loss**: Paper profit or loss not yet realized through sale
- **Tax Lot**: Individual purchase of a security with specific date and price
- **YTD**: Year-to-Date
- **ST/LT**: Short-term (<1 year) / Long-term (≥1 year) holding period

### B. References
- BNY UI/UX Design System v3.0
- FINRA Compliance Guidelines
- Market Data Provider API Documentation
- Custodian Integration Specifications

### C. Mockups and Wireframes
[To be provided by UX team]

### D. Test Scenarios
1. Load holdings for account with 500+ positions
2. Sort by each column in both directions
3. Apply multiple filters simultaneously
4. Export large dataset (1000+ holdings)
5. Verify real-time price updates
6. Test tax lot expansion for multi-lot positions
7. Validate calculations for various scenarios
8. Test accessibility with screen readers
9. Verify session timeout handling
10. Test error recovery mechanisms
