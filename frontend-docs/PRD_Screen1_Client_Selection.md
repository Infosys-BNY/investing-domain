# Product Requirements Document: Client Selection Screen

## 1. Executive Summary

### Product Overview
The Client Selection Screen is the primary entry point for BNY advisors to access client portfolio information. This screen enables advisors to search, filter, and select specific client accounts to view detailed holdings information.

### Business Objective
Provide BNY advisors with an efficient, secure, and intuitive interface to identify and select client accounts for portfolio management activities, specifically for viewing security holdings.

### Target Users
- Primary: BNY Financial Advisors
- Secondary: Senior Advisors, Portfolio Managers, Relationship Managers

## 2. User Stories

### Primary User Stories
1. **As a BNY advisor**, I want to quickly search for my clients by name or account number, so that I can access their portfolio information efficiently.
2. **As a BNY advisor**, I want to see key account metrics at a glance, so that I can prioritize which accounts need immediate attention.
3. **As a BNY advisor**, I want to filter my client list by various criteria, so that I can focus on specific client segments.
4. **As a BNY advisor**, I want to see my recently accessed clients, so that I can quickly return to accounts I'm actively managing.

## 3. Functional Requirements

### 3.1 Search Functionality
- **Search Bar**
  - Position: Top of screen, prominently displayed
  - Capabilities:
    - Search by client name (first, last, or full)
    - Search by account number
    - Search by tax ID (last 4 digits only for security)
  - Auto-complete suggestions after 3 characters
  - Clear button to reset search

### 3.2 Client List Display
- **List View Components:**
  - Client name (Last, First format)
  - Account ID/Number
  - Account Type (e.g., Individual, Joint, IRA, Trust, Corporate)
  - Total Market Value
  - Cash Balance
  - YTD Performance (percentage)
  - Last Activity Date
  - Advisor Assignment (if multiple advisors)

### 3.3 Filtering Options
- **Filter Panel** (collapsible sidebar):
  - Account Type filter (multi-select)
  - Market Value ranges (predefined brackets)
  - Performance filters (positive/negative/neutral)
  - Activity status (Active/Inactive/Dormant)
  - Risk Profile (Conservative/Moderate/Aggressive)
  - Date range for last activity

### 3.4 Sorting Options
- Sort by:
  - Client Name (A-Z, Z-A)
  - Market Value (High to Low, Low to High)
  - Recent Activity (Most Recent First)
  - Performance (Best to Worst)

### 3.5 Quick Actions
- **Primary Action Button**: "View Holdings" (prominent, right-aligned per row)
- **Secondary Actions** (via dropdown or icons):
  - View Client Profile
  - Contact Client
  - View Recent Transactions
  - Add to Watchlist

### 3.6 Recent Clients Section
- Display last 5-10 accessed clients
- Quick access cards with summary metrics
- One-click access to holdings

## 4. Non-Functional Requirements

### 4.1 Performance
- Initial page load: < 2 seconds
- Search results: < 500ms
- Client list pagination: 50 clients per page
- Smooth scrolling with lazy loading

### 4.2 Security
- Role-based access control (advisors see only assigned clients)
- Session timeout after 15 minutes of inactivity
- Audit logging for all client access
- Data encryption in transit (TLS 1.3)
- PII masking for sensitive data

### 4.3 Accessibility
- WCAG 2.1 AA compliance
- Keyboard navigation support
- Screen reader compatibility
- High contrast mode option

### 4.4 Responsiveness
- Desktop optimized (primary use case)
- Tablet support for field advisors
- Minimum resolution: 1366x768

## 5. User Interface Design

### 5.1 Layout Structure
```
+----------------------------------+
|        BNY Advisor Portal        |
|     [Search Bar]    [Filters]    |
+----------------------------------+
| Recent Clients                   |
| [Card] [Card] [Card] [Card]      |
+----------------------------------+
| All Clients          Sort: [▼]   |
|----------------------------------|
| ☐ John Smith                     |
|   Acct: XXX-1234  | $1.2M | +5%  |
|   Individual      | [View Holdings]|
|----------------------------------|
| ☐ Mary Johnson                   |
|   Acct: XXX-5678  | $850K | +2%  |
|   Joint          | [View Holdings]|
|----------------------------------|
| [Pagination: 1 2 3 ... 10]       |
+----------------------------------+
```

### 5.2 Visual Design Guidelines
- **Color Scheme**: BNY brand colors (primary blue #003087, secondary gray #6C757D)
- **Typography**: 
  - Headers: Roboto Medium 18px
  - Body: Roboto Regular 14px
  - Data: Roboto Mono 13px
- **Spacing**: 8px grid system
- **Icons**: Material Design Icons or custom BNY icon set

### 5.3 Interactive Elements
- Hover states for all clickable elements
- Loading spinners for data fetching
- Toast notifications for actions
- Smooth transitions (200ms ease-in-out)

## 6. Data Requirements

### 6.1 Data Sources
- Client Master Database
- Account Management System
- Portfolio Management System
- Performance Analytics Engine

### 6.2 Data Fields Required
| Field | Type | Source | Update Frequency |
|-------|------|--------|------------------|
| Client ID | String | Client Master | Real-time |
| Client Name | String | Client Master | Daily |
| Account Number | String | Account System | Real-time |
| Account Type | Enum | Account System | Real-time |
| Market Value | Decimal | Portfolio System | 15-min delay |
| Cash Balance | Decimal | Portfolio System | Real-time |
| YTD Performance | Percentage | Analytics | Daily |
| Last Activity | DateTime | Account System | Real-time |
| Risk Profile | Enum | Client Master | Weekly |

## 7. API Requirements

### 7.1 Endpoints Needed
- `GET /api/v1/advisor/{advisorId}/clients` - Retrieve client list
- `GET /api/v1/clients/search?query={searchTerm}` - Search clients
- `GET /api/v1/advisor/{advisorId}/recent-clients` - Get recent clients
- `POST /api/v1/audit/client-access` - Log client access

### 7.2 Response Format
```json
{
  "clients": [
    {
      "clientId": "CLT-123456",
      "clientName": "Smith, John",
      "accounts": [
        {
          "accountId": "ACC-789012",
          "accountType": "Individual",
          "marketValue": 1200000.00,
          "cashBalance": 50000.00,
          "ytdPerformance": 5.2,
          "lastActivity": "2024-10-30T14:30:00Z",
          "riskProfile": "Moderate"
        }
      ]
    }
  ],
  "totalCount": 150,
  "pageInfo": {
    "currentPage": 1,
    "totalPages": 3,
    "pageSize": 50
  }
}
```

## 8. Error Handling

### 8.1 Error Scenarios
- No search results found
- Network connectivity issues
- Session timeout
- Insufficient permissions
- Server errors

### 8.2 Error Messages
- User-friendly, actionable messages
- Error codes for support reference
- Suggested next steps
- Contact support option

## 9. Success Metrics

### 9.1 Key Performance Indicators
- Average time to find and select a client: < 10 seconds
- Search success rate: > 95%
- Page load time: < 2 seconds
- User satisfaction score: > 4.5/5

### 9.2 Usage Metrics to Track
- Number of searches per session
- Most used filters
- Click-through rate to holdings
- Time spent on screen
- Error rate

## 10. Future Enhancements

### Phase 2 Considerations
- Advanced search with boolean operators
- Saved search filters
- Bulk client selection
- Client grouping/portfolios
- Export client list functionality
- Mobile app version
- Voice search capability
- AI-powered client recommendations

## 11. Dependencies

### 11.1 Technical Dependencies
- Authentication Service
- Authorization Service
- Client Data Service
- Portfolio Management Service
- Analytics Service

### 11.2 Business Dependencies
- Advisor onboarding completion
- Client consent for data access
- Regulatory compliance approval

## 12. Acceptance Criteria

1. Advisor can search for a client and see results within 500ms
2. All assigned clients are visible in the list
3. Clicking "View Holdings" navigates to the Holdings Data View
4. Filters correctly narrow down the client list
5. Recent clients section shows last 10 accessed clients
6. All data is displayed accurately with proper formatting
7. Screen is accessible via keyboard navigation
8. Error states are handled gracefully with user-friendly messages

## 13. Appendix

### A. Glossary
- **UMA**: Unified Managed Account
- **YTD**: Year-to-Date
- **PII**: Personally Identifiable Information
- **Market Value**: Current total value of all securities in the account

### B. References
- BNY Brand Guidelines v2.0
- BNY Security Standards
- FINRA Compliance Requirements
- WCAG 2.1 Accessibility Guidelines
