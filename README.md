# BNY Advisor Portfolio Management Platform

<!-- Verification: Repository access and PR workflow test -->

## Overview

A modern web application designed for Bank of New York Mellon (BNY) financial advisors to efficiently manage client investment portfolios, analyze holdings, and identify optimization opportunities including tax loss harvesting strategies.

## Business Purpose

### Primary Objectives
- **Streamline Portfolio Analysis**: Enable advisors to quickly access and analyze client holdings with real-time market data
- **Tax Optimization**: Identify tax loss harvesting opportunities to minimize client tax liabilities
- **Enhanced Client Service**: Provide advisors with comprehensive portfolio insights for informed decision-making
- **Regulatory Compliance**: Ensure all portfolio management activities meet FINRA and SEC requirements

### Target Users
- BNY Financial Advisors
- Portfolio Managers
- Wealth Management Teams
- Tax Advisory Specialists

## Core Features

### Client Portfolio Access
- Secure client account selection with role-based access control
- Real-time portfolio valuation and performance metrics
- Multi-account management for complex client relationships

### Holdings Analysis
- Detailed security-level position data including:
  - Cost basis tracking
  - Unrealized gains/losses
  - Market value calculations
  - Tax lot management
- Advanced filtering and sorting capabilities
- Export functionality for offline analysis

### Tax Loss Harvesting
- Automated identification of tax loss opportunities
- Short-term vs. long-term capital gains analysis
- Tax-efficient rebalancing recommendations
- Year-end tax planning tools

## Technical Architecture

### Frontend Stack
- **Framework**: Angular 19 with standalone components
- **UI Components**: Angular Material 19
- **Styling**: TailwindCSS 3.4+
- **State Management**: Angular Signals API
- **Build Tool**: Angular CLI with esbuild

### Design Principles
- Mobile-responsive design for field advisors
- WCAG 2.1 AA accessibility compliance
- Real-time data synchronization
- Optimized performance with lazy loading

## Project Structure

```
investing-domain/
├── angular-frontend-guide.md   # Frontend development standards
├── screens.md                   # UI/UX specifications
└── src/                        # Application source code
```

## Getting Started

### Prerequisites
- Node.js 20.x LTS
- Angular CLI 19.0+
- npm 10.x or yarn 1.22+

### Installation
```bash
# Clone repository
git clone [repository-url]

# Install dependencies
npm install

# Start development server
ng serve

# Navigate to http://localhost:4200
```

### Development
```bash
# Run tests
ng test

# Build for production
ng build --configuration=production

# Run linting
ng lint
```

## Security & Compliance

- **Data Protection**: End-to-end encryption for all client data
- **Authentication**: Multi-factor authentication required
- **Audit Trail**: Comprehensive logging of all advisor actions
- **Regulatory**: FINRA Rule 2111 and SEC Rule 17a-4 compliant

## Support

For technical support or questions about the platform, please contact the BNY Technology Services team.

## License

Proprietary - Bank of New York Mellon Corporation. All rights reserved.
