package se.ifmo.lab3web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HitDetectorTest {

    private HitDetector hitDetector;

    @BeforeEach
    void setUp() {
        hitDetector = new HitDetector();
    }

    @Test
    void testOriginIsHit() {
        boolean result = hitDetector.identifyHit(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE);
        assertTrue(result, "Точка (0, 0) должна попадать в область при r=1");
    }

    @Test
    void testNegativeRadiusReturnsFalse() {
        boolean result = hitDetector.identifyHit(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(-1));
        assertFalse(result, "При отрицательном r результат всегда false");
    }

    @Test
    void testZeroRadiusReturnsFalse() {
        boolean result = hitDetector.identifyHit(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertFalse(result, "При r=0 результат всегда false");
    }

    @Test
    void testPointFarOutsideReturnsFalse() {
        boolean result = hitDetector.identifyHit(BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.ONE);
        assertFalse(result, "Очень далёкая точка не должна попадать в область");
    }

    @Test
    void testSmallPositiveRadius() {
        boolean result = hitDetector.identifyHit(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(0.5));
        assertTrue(result, "Точка (0, 0) должна попадать при любом положительном r");
    }
}
