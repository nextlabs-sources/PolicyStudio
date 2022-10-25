package com.bluejungle.destiny.policymanager.model;

import java.util.Collection;
import java.util.Date;

import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;

public final class TimeRelationHelper {

    private TimeRelationHelper() {
    }
    
    public static Date getLatestActiveFrom(Collection<DeploymentHistory> records) {
        assert records != null;
        assert !records.isEmpty();
        
        Date latest = null;
        
        for (DeploymentHistory de : records) {
            Date thisTime = de.getTimeRelation().getActiveFrom();
            if (latest == null) {
                latest = thisTime;
            } else if (latest.before(thisTime)) {
                latest = thisTime;
            }
        }
        return latest;
    }
}
