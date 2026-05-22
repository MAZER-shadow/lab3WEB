package se.ifmo.lab3web.jmx;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

public class PointStats extends NotificationBroadcasterSupport implements PointStatsMBean {

    private final AtomicLong totalPoints = new AtomicLong();
    private final AtomicLong hitsInArea = new AtomicLong();
    private final AtomicLong notifSeq = new AtomicLong();

    public void recordPoint(BigDecimal x, BigDecimal y, BigDecimal r, boolean hitStatus) {
        totalPoints.incrementAndGet();
        if (hitStatus) {
            hitsInArea.incrementAndGet();
        }
        if (isOutOfDisplayedArea(x, y, r)) {
            Notification n = new Notification(
                    "point.out_of_bounds",
                    this,
                    notifSeq.incrementAndGet(),
                    System.currentTimeMillis(),
                    "Point (" + x + ", " + y + ") is outside displayed area (R=" + r + ")"
            );
            sendNotification(n);
        }
    }

    @Override
    public long getTotalPoints() {
        return totalPoints.get();
    }

    @Override
    public long getHitsInArea() {
        return hitsInArea.get();
    }

    private static final BigDecimal THIRTY = BigDecimal.valueOf(30);
    private static final BigDecimal TWENTY_FIVE = BigDecimal.valueOf(25);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    private boolean isOutOfDisplayedArea(BigDecimal x, BigDecimal y, BigDecimal r) {
        BigDecimal maxCoord = THIRTY.multiply(r)
                .divide(TWENTY_FIVE.subtract(FOUR.multiply(r)), 10, RoundingMode.HALF_UP);
        return x.abs().compareTo(maxCoord) > 0 || y.abs().compareTo(maxCoord) > 0;
    }
}
