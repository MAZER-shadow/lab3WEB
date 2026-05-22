package se.ifmo.lab3web.jmx;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ApplicationScoped
public class JmxRegistrar {

    private static final Logger LOG = Logger.getLogger(JmxRegistrar.class.getName());
    private static final String DOMAIN = "se.ifmo.lab3web";

    private PointStats pointStats;
    private ClickInterval clickInterval;

    @PostConstruct
    public void register() {
        pointStats = new PointStats();
        clickInterval = new ClickInterval();

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName pointStatsName = new ObjectName(DOMAIN + ":type=PointStats");
            if (mbs.isRegistered(pointStatsName)) {
                mbs.unregisterMBean(pointStatsName);
            }
            mbs.registerMBean(pointStats, pointStatsName);

            ObjectName clickIntervalName = new ObjectName(DOMAIN + ":type=ClickInterval");
            if (mbs.isRegistered(clickIntervalName)) {
                mbs.unregisterMBean(clickIntervalName);
            }
            mbs.registerMBean(clickInterval, clickIntervalName);

            LOG.info("JMX MBeans registered: PointStats, ClickInterval");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to register JMX MBeans — monitoring will be unavailable", e);
        }
    }

    @PreDestroy
    public void unregister() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            mbs.unregisterMBean(new ObjectName(DOMAIN + ":type=PointStats"));
            mbs.unregisterMBean(new ObjectName(DOMAIN + ":type=ClickInterval"));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to unregister JMX MBeans", e);
        }
    }

    public PointStats getPointStats() {
        return pointStats;
    }

    public ClickInterval getClickInterval() {
        return clickInterval;
    }
}
