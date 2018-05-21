package pl.edu.agh.rmsp.service;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Created by Dominik on 08.12.15.
 */
public class DecisionService implements Serializable, DecisionServiceMBean {

    public static final String HOSTNAME_PROPERTY = "pl.edu.agh.rmsp.hostname";
    public static final String JMX_PORT_PROPERTY = "pl.edu.agh.rmsp.jmxport";

    private static final int DEFAULT_JMX_PORT = 33334;
    public static final String DEFAULT_HOSTNAME = "localhost";

    private static DecisionService slaveManager;

    private int decision;
    
    static {
        try {
            slaveManager = new DecisionService();
        } catch (Exception e) {
            System.err.println("ERROR!");
            e.printStackTrace();
        }
    }

    private DecisionService() throws Exception {
        registerMBean();
    }

    private void registerMBean() throws Exception {
        startMBeanServer(getHostname(), getJmxPort());
    }

    private void startMBeanServer(String hostname, int jmxPort) throws Exception {
        System.out.println("Starting MBean server: " + hostname + ":" + jmxPort);
        LocateRegistry.createRegistry(jmxPort);

        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        mbeanServer.registerMBean(this, new ObjectName("pl.edu.agh.rmsp:name=SlaveManager"));

        JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", hostname, jmxPort));
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, Collections.<String, Object>emptyMap(), mbeanServer);
        cs.start();
    }

    public static DecisionService getInstance() {
        return slaveManager;
    }

    @Override
    public int getDecision() {
       return decision;
    }

    @Override
    public String getId() {
        return "WG1";
    }

    private static int getJmxPort() {
        try {
            return Integer.parseInt(System.getProperty(JMX_PORT_PROPERTY, Integer.toString(DEFAULT_JMX_PORT)));
        } catch (Exception e) {
            System.out.println("Can't determine value of port basing on user input! Using default jmx port: " + DEFAULT_JMX_PORT);
        }
        return DEFAULT_JMX_PORT;
    }

    private static String getHostname() {
        return System.getProperty(HOSTNAME_PROPERTY, DEFAULT_HOSTNAME);
    }

    @Override
	public void setDecision(int decision) {
		this.decision = decision;
    }
    
}