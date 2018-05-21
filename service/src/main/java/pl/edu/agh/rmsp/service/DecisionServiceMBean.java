package pl.edu.agh.rmsp.service;

/**
 * Created by Dominik on 08.12.15.
 */
public interface DecisionServiceMBean {

    String getId();

    int getDecision();
    
    void setDecision(int i);
}
