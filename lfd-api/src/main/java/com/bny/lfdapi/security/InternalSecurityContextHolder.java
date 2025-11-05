package com.bny.lfdapi.security;

public class InternalSecurityContextHolder {
    
    private static final ThreadLocal<InternalRequestContext> contextHolder = new ThreadLocal<>();
    
    public static void setContext(InternalRequestContext context) {
        contextHolder.set(context);
    }
    
    public static InternalRequestContext getContext() {
        return contextHolder.get();
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
}
