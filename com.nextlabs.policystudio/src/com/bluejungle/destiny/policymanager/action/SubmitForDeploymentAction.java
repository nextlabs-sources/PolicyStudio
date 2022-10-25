/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import com.bluejungle.destiny.policymanager.event.IPolicyOrComponentData;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PredicateHelpers;
import com.bluejungle.destiny.policymanager.ui.dialogs.DACSubmitCheckDependenciesDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.SubmitCheckDependenciesDialog;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * @author fuad
 * @version $Id$
 */

public class SubmitForDeploymentAction extends BaseDisableableAction {

    public SubmitForDeploymentAction() {
        super();
    }

    public SubmitForDeploymentAction(String text) {
        super(text);
    }

    public SubmitForDeploymentAction(String text, ImageDescriptor image) {
        super(text, image);
    }

    /**
     * Constructor
     * 
     * @param text
     * @param style
     */
    public SubmitForDeploymentAction(String text, int style) {
        super(text, style);
    }

    protected List<DomainObjectDescriptor> objectList = null;
    @Override
    public void run() {
        GlobalState gs = GlobalState.getInstance();
        objectList = new ArrayList<DomainObjectDescriptor>();

		Set<DomainObjectDescriptor> selectedItems = gs.getCurrentSelection();
        Collection<? extends IHasId> domainObjectList = PolicyServerProxy.getEntitiesForDescriptor(selectedItems);
        for(IHasId domainObject: domainObjectList){
        	if(!contentValidation(domainObject)){
        		return;
        	}
        }
        SubmitCheckDependenciesDialog dlg = new SubmitCheckDependenciesDialog(
				Display.getCurrent().getActiveShell()
				, objectList
				, SubmitCheckDependenciesDialog.SUBMIT);
		dlg.open();
    }

    public boolean contentValidation(IHasId domainObject){
    	String policyName = DomainObjectHelper.getDisplayName(domainObject);
    	if(PolicyHelpers.isDACPolicyType(domainObject) && !PolicyHelpers.isSelectedADEnforcer (domainObject)){
        	displayErrorDialog(
        			"No deployment target"
                    , policyName + " \nThis object has not selected a deployment target. You can not submit this object for deployment.");
            return false;
    	}
    	DevelopmentStatus status = DomainObjectHelper.getStatus(domainObject);
    	// Check consistency
		for (EffectType effectType : EffectType.elements()) {
			if (domainObject instanceof IDPolicy) {
				Collection<IObligation> obligations = ((IDPolicy) domainObject).getObligations(effectType);
		        for (IObligation obligation : obligations) {
		        	if (obligation.getType().equals("notify")) {
		        		String email = ((NotifyObligation)obligation).getEmailAddresses();
		                if (email.length() == 0) {
		                	displayErrorDialog(
		                			"No recipients specified"
		                			, policyName + " \nThere are no recipients specified for one or more Send Email obligations.");
		                    return false;
		                }
		                if (!isValidEmailAddress(email)){ 
		                	displayErrorDialog(
		                			"Wrong email format"
		                			, policyName + " \nThe email format is incorrect.");
		                    return false;
		                }
		        	}
		        }
			}
		}
		if (status == DevelopmentStatus.NEW|| status == DevelopmentStatus.EMPTY) {
			// TODO: make sure that empty status is removed when appropriate.
			DomainObjectHelper.setStatus(domainObject, DevelopmentStatus.DRAFT);
	    }

		if (PolicyServerProxy.saveEntity(domainObject) == null) {
			displayErrorDialog(
	              "Error Saving Entity"
					, policyName + " \nThe object could not be saved. There may be an error in the object." 
	            +	" Please correct the problem and try again." 
	            + " If the problem persists, pleas" +
	            		"e contact your System Administrator.");
            return false;
		}

		if (domainObject instanceof IDPolicy) {
			IDPolicy policy = (IDPolicy) domainObject;
			DODDigest dod = EntityInfoProvider.getPolicyDescriptor(policy.getName());
			DomainObjectDescriptor des;
			try {
				des = PolicyServerProxy.getDescriptorById(dod.getId());
			} catch (PolicyEditorException e) {
				e.printStackTrace();
	            return false;
			}
			objectList.add(des);
			// Add validation for policy start and end times. In future, create
			// full validation framework
			IPredicate conditions = policy.getConditions();
			Long startTimeAsLong = extractPolicyStartTime(conditions);
			Long endTimeAsLong = extractPolicyEndtime(conditions);
	
			long currentTime = System.currentTimeMillis();
			if ((endTimeAsLong != null)&& (endTimeAsLong.longValue() < currentTime)) {
				displayErrorDialog(
	                  "Invalid Policy"
	                  , policyName + " \nPlease verify that the end time condition of the policy falls after the current time.");
	            return false;
			}
			if ((endTimeAsLong != null)&& (startTimeAsLong != null)&& (startTimeAsLong.longValue() > endTimeAsLong.longValue())) {
				displayErrorDialog(
	                  "Invalid Policy"
	                  , policyName + " \nPlease verify that the policy end time condition falls after the start time condition.");
	            return false;
			}
		} else if (domainObject instanceof IDSpec) {
			// Validation check for action component
			String name = ((IDSpec) domainObject).getName();
			DODDigest dod = EntityInfoProvider.getComponentDescriptor(name);
			DomainObjectDescriptor des;
			try {
				des = PolicyServerProxy.getDescriptorById(dod.getId());
			} catch (PolicyEditorException e) {
				e.printStackTrace();
	            return false;
			}
			objectList.add(des);
		}
		return true ;
    }




    /**
     * Email format check
     * @param email
     * @return
     */
    public static boolean isValidEmailAddress(String email) {
    	boolean result = true;
    	try {
    		InternetAddress emailAddr = new InternetAddress(email);
    		emailAddr.validate();
    	} catch (AddressException ex) {
    		result = false;
    	}
    	return result;
	}
    /**
     * Extract the policy end time from the specified conditions
     * 
     * @param conditions
     * @return the policy end time if it exists; null otherwise
     */
    private Long extractPolicyEndtime(IPredicate conditions) {
        Long endTimeAsLong = null;
        Relation endTime = (Relation) PredicateHelpers.getEndTime(conditions);
        if (endTime != null) {
            endTimeAsLong = (Long) endTime.getRHS().evaluate(null).getValue();
        }
        return endTimeAsLong;
    }

    /**
     * Extract the policy start time from the specified conditions
     * 
     * @param conditions
     * @return the policy start time if it exists; null otherwise
     */
    private Long extractPolicyStartTime(IPredicate conditions) {
        Long startTimeAsLong = null;
        Relation startTime = (Relation) PredicateHelpers.getStartTime(conditions);
        if (startTime != null) {
            startTimeAsLong = (Long) startTime.getRHS().evaluate(null).getValue();
        }
        return startTimeAsLong;
    }

    /**
     * Display an error dialog
     * 
     * @param errorMessageSummary
     * @param errorMessageDetail
     */
    private void displayErrorDialog(String errorMessageSummary,String errorMessageDetail) {
        MessageDialog.openError(
                Display.getCurrent().getActiveShell()
              , errorMessageSummary
              , errorMessageDetail
        );
    }

    @Override
    public void refreshEnabledState(Set<IPolicyOrComponentData> selectedItems) {
        boolean newState = false;

		for(IPolicyOrComponentData item: selectedItems ){
            IHasId selectedEntity = item.getEntity();
            if (selectedEntity instanceof PolicyFolder) {
                newState = false;
            } else {
                DevelopmentStatus status = DomainObjectHelper.getStatus(selectedEntity);
                newState = (status == DevelopmentStatus.NEW
                         || status == DevelopmentStatus.DRAFT 
                         || status == DevelopmentStatus.EMPTY);
                newState &= PolicyServerProxy.canPerformAction(selectedEntity, DAction.APPROVE);
            }
            if(!newState){
				break;
			}
        }
        setEnabled(newState);
    }
}
