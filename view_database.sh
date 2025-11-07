#!/bin/bash

################################################################################
# BNY Data Services - Database Viewer
# Displays database contents in a readable format using MySQL client
################################################################################

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Database connection defaults
# Extract from DATABASE_URL if available, otherwise use hardcoded defaults
if [ -n "$DATABASE_URL" ]; then
    # Parse jdbc:mysql://host:port/database
    DB_FROM_URL=$(echo "$DATABASE_URL" | sed -n 's|jdbc:mysql://\([^:]*\):\([0-9]*\)/\(.*\)|\1 \2 \3|p')
    DEFAULT_HOST=$(echo "$DB_FROM_URL" | awk '{print $1}')
    DEFAULT_PORT=$(echo "$DB_FROM_URL" | awk '{print $2}')
    DEFAULT_DATABASE=$(echo "$DB_FROM_URL" | awk '{print $3}')
else
    DEFAULT_HOST="bny-demo.c3uyq60ukgb6.us-east-2.rds.amazonaws.com"
    DEFAULT_PORT="3306"
    DEFAULT_DATABASE="bny_data_services"
fi

DEFAULT_USER="${DATABASE_USERNAME:-admin}"

# Print separator line
print_separator() {
    local char="${1:-=}"
    printf '%*s\n' 80 '' | tr ' ' "$char"
}

# Print section header
print_header() {
    echo ""
    print_separator "="
    echo -e "${BOLD}  $1${NC}"
    print_separator "="
    echo ""
}

# Print error message
print_error() {
    echo -e "${RED}✗ Error: $1${NC}"
}

# Print success message
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Check if mysql client is installed
check_mysql_client() {
    if ! command -v mysql &> /dev/null; then
        print_error "MySQL client is not installed"
        echo ""
        echo "Install it using:"
        echo "  macOS:   brew install mysql-client"
        echo "  Ubuntu:  sudo apt-get install mysql-client"
        echo "  CentOS:  sudo yum install mysql"
        exit 1
    fi
}

# Get database credentials
get_credentials() {
    echo ""
    print_separator "="
    echo -e "${BOLD}  BNY Data Services - Database Viewer${NC}"
    print_separator "="
    echo ""
    
    # Use environment variables directly
    DB_HOST="$DEFAULT_HOST"
    DB_USER="$DEFAULT_USER"
    
    # Get password - use env var if available
    if [ -n "$DATABASE_PASSWORD" ]; then
        DB_PASSWORD="$DATABASE_PASSWORD"
        echo "Connecting to: $DB_HOST"
        echo "User: $DB_USER"
        echo "Password: (using environment variable)"
        echo ""
    else
        echo "Connecting to: $DB_HOST"
        echo "User: $DB_USER"
        read -s -p "Database Password: " DB_PASSWORD
        echo ""
        echo ""
    fi
    
    # Build connection string
    MYSQL_CMD="mysql -h $DB_HOST -P $DEFAULT_PORT -u $DB_USER -p$DB_PASSWORD $DEFAULT_DATABASE"
}

# Test database connection
test_connection() {
    if $MYSQL_CMD -e "SELECT 1" 2>/dev/null >/dev/null; then
        print_success "Connected to $DEFAULT_DATABASE database"
        return 0
    else
        print_error "Failed to connect to database"
        return 1
    fi
}

# Display database summary
display_summary() {
    print_header "DATABASE SUMMARY"
    
    # Get counts
    local client_count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM clients")
    local account_count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM accounts")
    local holding_count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM holdings")
    local security_count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM securities")
    
    # Get totals
    local totals=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT SUM(market_value), SUM(cash_balance) FROM accounts")
    local total_mv=$(echo "$totals" | awk '{print $1}')
    local total_cash=$(echo "$totals" | awk '{print $2}')
    local total_aum=$(echo "$total_mv + $total_cash" | bc)
    
    echo "Record Counts:"
    printf "  Clients:     %'d\n" "$client_count"
    printf "  Accounts:    %'d\n" "$account_count"
    printf "  Holdings:    %'d\n" "$holding_count"
    printf "  Securities:  %'d\n" "$security_count"
    
    echo ""
    echo "Total Values:"
    printf "  Market Value: \$%'0.2f\n" "$total_mv"
    printf "  Cash Balance: \$%'0.2f\n" "$total_cash"
    printf "  Total AUM:    \$%'0.2f\n" "$total_aum"
    echo ""
}

# Display securities
display_securities() {
    print_header "SECURITIES"
    
    local count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM securities")
    echo "Total Securities: $count"
    echo ""
    
    # Get securities data
    $MYSQL_CMD 2>/dev/null -sN -e "
        SELECT symbol, security_name, IFNULL(sector, 'N/A'), asset_class,
               current_price, price_change, price_change_percent,
               DATE_FORMAT(last_price_update, '%Y-%m-%d %H:%i:%s')
        FROM securities
        ORDER BY asset_class, symbol
    " | while IFS=$'\t' read -r symbol name sector asset_class price change change_pct updated; do
        
        # Format price change with color
        local change_str
        if (( $(echo "$change >= 0" | bc -l) )); then
            change_str="${GREEN}+\$$(printf '%0.2f' $change)${NC}"
        else
            change_str="${RED}\$$(printf '%0.2f' $change)${NC}"
        fi
        
        local change_pct_str
        if (( $(echo "$change_pct >= 0" | bc -l) )); then
            change_pct_str="${GREEN}+$(printf '%0.2f' $change_pct)%${NC}"
        else
            change_pct_str="${RED}$(printf '%0.2f' $change_pct)%${NC}"
        fi
        
        echo -e "${BOLD}$symbol${NC} - $name"
        printf "  %-15s %s\n" "Sector:" "$sector"
        printf "  %-15s %s\n" "Asset Class:" "$asset_class"
        printf "  %-15s \$%'0.2f  " "Price:" "$price"
        echo -e "($change_str / $change_pct_str)"
        printf "  %-15s %s\n" "Last Updated:" "$updated"
        echo ""
    done
}

# Display clients with accounts
display_clients_accounts() {
    print_header "CLIENTS & ACCOUNTS"
    
    local client_count=$($MYSQL_CMD 2>/dev/null -sN -e "SELECT COUNT(*) FROM clients")
    echo "Total Clients: $client_count"
    echo ""
    
    # Get all clients
    $MYSQL_CMD 2>/dev/null -sN -e "
        SELECT client_id, client_name, advisor_id, IFNULL(tax_id, 'N/A'), 
               DATE_FORMAT(created_date, '%Y-%m-%d %H:%i:%s')
        FROM clients
        ORDER BY client_name
    " | while IFS=$'\t' read -r client_id client_name advisor_id tax_id created_date; do
        echo -e "${BOLD}${CYAN}$client_name${NC} ${BOLD}($client_id)${NC}"
        print_separator "-" 60
        printf "  %-15s %s\n" "Advisor:" "$advisor_id"
        printf "  %-15s %s\n" "Tax ID:" "$tax_id"
        printf "  %-15s %s\n" "Client Since:" "$created_date"
        
        # Get accounts for this client
        local account_data=$($MYSQL_CMD 2>/dev/null -sN -e "
            SELECT account_number, account_type, market_value, cash_balance, 
                   ytd_performance, IFNULL(risk_profile, 'N/A')
            FROM accounts
            WHERE client_id = '$client_id'
            ORDER BY account_number
        ")
        
        if [ -n "$account_data" ]; then
            local acc_count=$(echo "$account_data" | wc -l | xargs)
            echo ""
            echo -e "  ${BOLD}Accounts ($acc_count):${NC}"
            echo ""
            
            echo "$account_data" | while IFS=$'\t' read -r acc_num acc_type mv cash ytd risk; do
                echo -e "    ${YELLOW}●${NC} $acc_num - $acc_type"
                printf "      %-18s \$%'15.2f\n" "Market Value:" "$mv"
                printf "      %-18s \$%'15.2f\n" "Cash Balance:" "$cash"
                printf "      %-18s %15.1f%%\n" "YTD Performance:" "$ytd"
                printf "      %-18s %15s\n" "Risk Profile:" "$risk"
                echo ""
            done
        else
            echo ""
            echo "  (No accounts)"
        fi
        
        echo ""
    done
}

# Display holdings by account
display_holdings() {
    print_header "HOLDINGS BY ACCOUNT"
    
    # Get all accounts with client info
    $MYSQL_CMD 2>/dev/null -sN -e "
        SELECT a.account_id, a.account_number, c.client_name
        FROM accounts a
        JOIN clients c ON a.client_id = c.client_id
        ORDER BY c.client_name, a.account_number
    " | while IFS=$'\t' read -r account_id account_number client_name; do
        
        # Get holdings for this account
        local holdings=$($MYSQL_CMD 2>/dev/null -sN -e "
            SELECT h.symbol, s.security_name, h.quantity, h.cost_basis,
                   s.current_price, DATE_FORMAT(h.purchase_date, '%Y-%m-%d %H:%i:%s')
            FROM holdings h
            JOIN securities s ON h.symbol = s.symbol
            WHERE h.account_id = '$account_id'
            ORDER BY h.symbol
        ")
        
        if [ -n "$holdings" ]; then
            echo ""
            echo -e "${YELLOW}$client_name - $account_number${NC}"
            print_separator "-" 60
            
            local total_cost=0
            local total_market=0
            
            echo "$holdings" | while IFS=$'\t' read -r symbol name qty cost price purchase; do
                # Calculate values
                local market_value=$(echo "$qty * $price" | bc -l)
                local gain_loss=$(echo "$market_value - $cost" | bc -l)
                local gain_loss_pct=$(echo "scale=2; ($gain_loss / $cost) * 100" | bc -l)
                
                total_cost=$(echo "$total_cost + $cost" | bc -l)
                total_market=$(echo "$total_market + $market_value" | bc -l)
                
                echo ""
                echo "  $symbol - $name"
                printf "    Quantity:      %'0.4f\n" "$qty"
                printf "    Cost Basis:    \$%'0.2f\n" "$cost"
                printf "    Current Price: \$%'0.2f\n" "$price"
                printf "    Market Value:  \$%'0.2f\n" "$market_value"
                
                if (( $(echo "$gain_loss >= 0" | bc -l) )); then
                    printf "    Gain/Loss:     ${GREEN}\$%'0.2f (+%0.2f%%)${NC}\n" "$gain_loss" "$gain_loss_pct"
                else
                    printf "    Gain/Loss:     ${RED}\$%'0.2f (%0.2f%%)${NC}\n" "$gain_loss" "$gain_loss_pct"
                fi
                
                echo "    Purchased:     $purchase"
            done
            
            # Calculate account totals
            local account_totals=$($MYSQL_CMD 2>/dev/null -sN -e "
                SELECT 
                    SUM(h.cost_basis),
                    SUM(h.quantity * s.current_price)
                FROM holdings h
                JOIN securities s ON h.symbol = s.symbol
                WHERE h.account_id = '$account_id'
            ")
            
            local acc_cost=$(echo "$account_totals" | awk '{print $1}')
            local acc_market=$(echo "$account_totals" | awk '{print $2}')
            local acc_gain=$(echo "$acc_market - $acc_cost" | bc -l)
            local acc_gain_pct=$(echo "scale=2; ($acc_gain / $acc_cost) * 100" | bc -l)
            
            echo ""
            echo "  ACCOUNT TOTALS:"
            printf "    Total Cost:        \$%'0.2f\n" "$acc_cost"
            printf "    Total Market:      \$%'0.2f\n" "$acc_market"
            
            if (( $(echo "$acc_gain >= 0" | bc -l) )); then
                printf "    Total Gain/Loss:   ${GREEN}\$%'0.2f (+%0.2f%%)${NC}\n" "$acc_gain" "$acc_gain_pct"
            else
                printf "    Total Gain/Loss:   ${RED}\$%'0.2f (%0.2f%%)${NC}\n" "$acc_gain" "$acc_gain_pct"
            fi
            
            echo ""
        fi
    done
}

# Main execution
main() {
    # Check prerequisites
    check_mysql_client
    
    # Get credentials
    get_credentials
    
    # Test connection
    if ! test_connection; then
        exit 1
    fi
    
    # Display all data
    display_summary
    display_securities
    display_clients_accounts
    display_holdings
    
    echo ""
    print_success "Database view complete"
    echo ""
}

# Run main function
main
