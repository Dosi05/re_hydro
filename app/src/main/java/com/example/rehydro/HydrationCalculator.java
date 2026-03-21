package com.example.rehydro;

import java.util.List;

public class HydrationCalculator {

    // Widmark body water ratio by sex
    private static final float R_MALE   = 0.68f;
    private static final float R_FEMALE = 0.55f;

    // Alcohol density g/ml
    private static final double ALCOHOL_DENSITY = 0.789;

    // ml of water needed per gram of pure alcohol consumed
    private static final double WATER_PER_GRAM = 8.0;

    /**
     * BAC using Widmark formula.
     * Returns g/100ml — legal limit is typically 0.08.
     */
    public static float calculateBAC(List<DrinkEntry> drinks,
                                     float weightKg, String sex) {
        double pureAlcoholMl = 0;
        for (DrinkEntry d : drinks) {
            if (!d.isWater) {
                pureAlcoholMl += (d.abvPercent / 100.0) * d.volumeMl;
            }
        }
        if (pureAlcoholMl == 0) return 0f;

        double alcoholGrams = pureAlcoholMl * ALCOHOL_DENSITY;
        float r = "F".equals(sex) ? R_FEMALE : R_MALE;

        // Widmark: BAC (g/100ml) = alcoholGrams / (r × weightKg × 10)
        return (float) (alcoholGrams / (r * weightKg * 10));
    }

    /**
     * Returns drunkenness as 0.0 to 1.0.
     * Mapped so BAC 0.25 = fully drunk (1.0).
     */
    public static float getDrunkenessLevel(List<DrinkEntry> drinks,
                                           float weightKg, String sex) {
        float bac = calculateBAC(drinks, weightKg, sex);
        // 0.25 g/100ml = very drunk threshold
        return Math.min(bac / 0.25f, 1.0f);
    }

    /**
     * Total ml of water needed to process all alcohol consumed.
     */
    public static int calculateWaterNeededMl(List<DrinkEntry> drinks) {
        double pureAlcoholMl = 0;
        for (DrinkEntry d : drinks) {
            if (!d.isWater) {
                pureAlcoholMl += (d.abvPercent / 100.0) * d.volumeMl;
            }
        }
        double grams = pureAlcoholMl * ALCOHOL_DENSITY;
        return (int) Math.round(grams * WATER_PER_GRAM);
    }

    /**
     * Hydration percentage — how much of needed water has been consumed.
     * Capped at 100%.
     */
    public static float hydrationPercent(int waterNeededMl, int waterConsumedMl) {
        if (waterNeededMl == 0) return 100f;
        return Math.min((waterConsumedMl / (float) waterNeededMl) * 100f, 100f);
    }
}