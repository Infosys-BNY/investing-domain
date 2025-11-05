package com.bny.shared.util.transformation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class CurrencyFormatter {
    
    private static final int DEFAULT_SCALE = 2;
    private static final NumberFormat USD_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    
    public String formatUSD(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return USD_FORMAT.format(amount);
    }
    
    public BigDecimal round(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }
    
    public BigDecimal round(BigDecimal amount, int scale) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }
}
