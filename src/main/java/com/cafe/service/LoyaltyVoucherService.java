package com.cafe.service;

/** Pure calculation used by both checkout validation and the loyalty UI. */
public final class LoyaltyVoucherService {
    public static final double MAX_DISCOUNT_RATE = 0.80;

    private LoyaltyVoucherService() {}

    public static VoucherQuote quote(String role, double orderTotal, int requestedPoints,
                                     int availablePoints, int vndPerPoint) {
        boolean member = role != null && "member".equalsIgnoreCase(role.trim());
        int safePointValue = Math.max(1, vndPerPoint);
        int maxByOrder = (int) Math.floor(orderTotal * MAX_DISCOUNT_RATE / safePointValue);
        int usable = member
                ? Math.max(0, Math.min(requestedPoints, Math.min(availablePoints, maxByOrder)))
                : 0;
        double discount = usable * (double) safePointValue;
        return new VoucherQuote(usable, discount, Math.max(0, orderTotal - discount),
                Math.max(0, Math.min(availablePoints, maxByOrder)));
    }

    public record VoucherQuote(int pointsUsed, double discountAmount,
                               double netTotal, int maximumUsablePoints) {}
}
