package search;

import java.math.BigDecimal;

public record SearchCriteria(
        String district,
        String cuisine,
        Double minRating,
        BigDecimal priceCeiling
) {}