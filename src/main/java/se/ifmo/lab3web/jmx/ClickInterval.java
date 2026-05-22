package se.ifmo.lab3web.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClickInterval implements ClickIntervalMBean {

    private final List<Long> clickTimestamps = Collections.synchronizedList(new ArrayList<>());

    public void recordClick() {
        clickTimestamps.add(System.currentTimeMillis());
    }

    @Override
    public double getAverageIntervalMs() {
        List<Long> times = new ArrayList<>(clickTimestamps);
        if (times.size() < 2) {
            return 0.0;
        }
        long total = 0;
        for (int i = 1; i < times.size(); i++) {
            total += times.get(i) - times.get(i - 1);
        }
        return (double) total / (times.size() - 1);
    }

    @Override
    public long getClickCount() {
        return clickTimestamps.size();
    }
}
