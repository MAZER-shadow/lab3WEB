package se.ifmo.lab3web.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.math.MathContext;

@Named
@ApplicationScoped
public class HitDetector {

    private static final MathContext MC = new MathContext(20);
    private static final BigDecimal SEVEN = BigDecimal.valueOf(7);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal THREE = BigDecimal.valueOf(3);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);
    private static final BigDecimal ONE_POINT_FIVE = BigDecimal.valueOf(1.5);
    private static final BigDecimal THREE_POINT_THREE = THREE.multiply(new BigDecimal(Math.sqrt(33.0), MC));
    private static final BigDecimal DENOMINATOR = BigDecimal.valueOf(112);
    private static final BigDecimal FACTOR = THREE_POINT_THREE.subtract(SEVEN).divide(DENOMINATOR, MC);
    private static final BigDecimal ZERO_POINT_SIX = BigDecimal.valueOf(0.6);
    private static final BigDecimal ZERO_POINT_NINE = BigDecimal.valueOf(0.9);
    private static final BigDecimal ONE_POINT_FIVE_MINUS_ABS_X = ONE_POINT_FIVE.multiply(BigDecimal.valueOf(0.3));
    private static final BigDecimal ZERO_POINT_SEVEN_FIVE_SQUARED = BigDecimal.valueOf(0.75).pow(2);
    private static final BigDecimal ZERO_POINT_FOUR = BigDecimal.valueOf(0.4);
    private static final BigDecimal ZERO_POINT_ONE_FIVE_SQUARED = BigDecimal.valueOf(0.15).pow(2);
    private static final BigDecimal TWO_POINT_EIGHT = BigDecimal.valueOf(2.8);

    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(Math.sqrt(value.doubleValue()), MC);
    }

    private BigDecimal abs(BigDecimal value) {
        return value.abs();
    }

    private BigDecimal batmanYHigh(BigDecimal absX) {
        if (absX.compareTo(SEVEN) > 0) return BigDecimal.ZERO;

        if (absX.compareTo(THREE) >= 0 && absX.compareTo(SEVEN) <= 0) {
            BigDecimal term = absX.divide(SEVEN, MC).pow(2, MC);
            return THREE.multiply(sqrt(BigDecimal.ONE.subtract(term)));
        }

        if (absX.compareTo(ONE_POINT_FIVE) >= 0 && absX.compareTo(THREE) < 0) {
            BigDecimal dx = absX.subtract(BigDecimal.valueOf(2.25));
            BigDecimal term = dx.pow(2, MC).divide(ZERO_POINT_SEVEN_FIVE_SQUARED, MC);
            return TWO.subtract(sqrt(BigDecimal.ONE.subtract(term)));
        }

        if (absX.compareTo(ZERO_POINT_SIX) >= 0 && absX.compareTo(ONE_POINT_FIVE) < 0) {
            if (absX.compareTo(ZERO_POINT_NINE) <= 0) {
                BigDecimal dx = absX.subtract(BigDecimal.valueOf(0.75));
                BigDecimal term = dx.pow(2, MC).divide(ZERO_POINT_ONE_FIVE_SQUARED, MC);
                return THREE.add(ZERO_POINT_FOUR.multiply(sqrt(BigDecimal.ONE.subtract(term))));
            } else {
                return TWO.add(ONE_POINT_FIVE.subtract(absX).multiply(BigDecimal.valueOf(0.3)));
            }
        }

        return TWO_POINT_EIGHT;
    }

    private BigDecimal batmanYLow(BigDecimal absX) {
        if (absX.compareTo(SEVEN) > 0) return BigDecimal.ZERO;

        if (absX.compareTo(FOUR) >= 0 && absX.compareTo(SEVEN) <= 0) {
            BigDecimal term = absX.divide(SEVEN, MC).pow(2, MC);
            return THREE.negate().multiply(sqrt(BigDecimal.ONE.subtract(term)));
        }

        BigDecimal term1 = absX.divide(TWO, MC);

        BigDecimal term2 = FACTOR.multiply(absX.pow(2, MC));

        BigDecimal innerAbs = abs(absX.subtract(TWO));
        BigDecimal innerPow = innerAbs.subtract(BigDecimal.ONE).pow(2, MC);
        BigDecimal term3 = sqrt(BigDecimal.ONE.subtract(innerPow));

        return term1.subtract(term2).subtract(THREE).add(term3);
    }

    public boolean identifyHit(BigDecimal x, BigDecimal y, BigDecimal r) {
        if (r.compareTo(BigDecimal.ZERO) <= 0) return false;

        BigDecimal sevenOverR = SEVEN.divide(r, MC);
        BigDecimal xNorm = x.multiply(sevenOverR);
        BigDecimal yNorm = y.multiply(sevenOverR);

        BigDecimal absXNorm = abs(xNorm);

        if (absXNorm.compareTo(SEVEN) > 0) {
            return false;
        }

        BigDecimal yHigh = batmanYHigh(absXNorm);
        BigDecimal yLow = batmanYLow(absXNorm);

        return yNorm.compareTo(yLow) >= 0 && yNorm.compareTo(yHigh) <= 0;
    }
}