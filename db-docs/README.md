# BNY Data Services Database

## Purpose

Simple database to feed data to the core BNY application and display in the UI.

## Architecture

```
┌─────────┐    ┌──────────┐    ┌─────────────┐    ┌──────────┐
│  API    │───▶│ Stored   │───▶│   Database  │◀───│   Tables │
│ Layer   │    │ Procedures│   │   (MySQL)   │    │clients,  │
└─────────┘    └──────────┘    └─────────────┘    │accounts, │
                                                  │holdings, │
                                                  │securities│
                                                  └──────────┘
    ```

**Flow**: API calls stored procedures → stored procedures query database → return data to API

## Entity Relationships

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    clients  │       │   accounts  │       │   holdings  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ client_id (PK) │◄──────│ account_id (PK) │◄──────│ holding_id (PK) │
│ client_name │       │ client_id (FK) │       │ account_id (FK) │
│ advisor_id  │       │ account_number│       │ symbol (FK)     │
│ tax_id      │       │ account_type │       │ quantity        │
└─────────────┘       │ market_value │       │ cost_basis      │
         │             │ cash_balance │       └─────────────┘
         │             └─────────────┘                │
         │                      │                     │
         │                      │                     │
┌─────────────┐                │                ┌─────────────┐
│  securities  │                │                │  securities  │
├─────────────┤                │                ├─────────────┤
│ symbol (PK)  │◄────────────────┘                │ symbol (PK)  │
│ security_name│                                  │ security_name│
│ sector       │                                  │ sector       │
│ asset_class  │                                  │ asset_class  │
└─────────────┘                                  └─────────────┘
```

## Database Connection

- **Endpoint**: `bny-demo.c3uyqos ogb.us-east-2.rds.amazonaws.com`
- **Port**: `3306`
- **Database**: `bny_data_services`

## Quick Setup

1. **Create Database**
   ```sql
   CREATE DATABASE bny_data_services CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE bny_data_services;
   ```

2. **Run Setup Scripts**
   ```bash
   mysql -h bny-demo.c3uyq o.us-east-2.rds.amazonaws.com -P 3306 -u admin -p bny_data_services < 02-table-definitions.sql
   mysql -h bny-demo.c3uyq o.us-east-2.rds.amazonaws.com -P 3306 -u admin -p bny_data_services < 03-stored-procedures.sql
   mysql -h bny-demo.c3uyq o.us-east-2.rds.amazonaws.com -P 3306 -u admin -p bny_data_services < 05-seed-data.sql
   ```

3. **Verify Data**
   ```sql
   SELECT COUNT(*) as clients FROM clients;
   SELECT COUNT(*) as accounts FROM accounts;
   SELECT COUNT(*) as holdings FROM holdings;
   ```

4. **Test Stored Procedures**
   ```sql
   -- Test client procedure
   CALL sp_get_clients('ADV001');
   
   -- Test holdings procedure  
   CALL sp_get_holdings('ACC001');
   ```

## Files

- **[02-table-definitions.sql](./02-table-definitions.sql)** - Table schema
- **[03-stored-procedures.sql](./03-stored-procedures.sql)** - Simple stored procedures for API bridge
- **[05-seed-data.sql](./05-seed-data.sql)** - Sample data for UI

## Key Tables

- **clients** - Client information
- **accounts** - Account details  
- **holdings** - Portfolio holdings
- **securities** - Security reference data

## Stored Procedures

- **sp_get_clients** - Get clients for an advisor
- **sp_get_holdings** - Get holdings for an account

These procedures demonstrate the API → stored procedures → database bridge concept.
