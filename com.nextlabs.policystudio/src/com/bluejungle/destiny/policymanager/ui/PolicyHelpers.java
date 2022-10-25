/*
 * Created on May 11, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.destiny.services.policy.types.PolicyServiceFault;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.destiny.policymanager.action.PolicyStudioActionFactory;
import com.bluejungle.destiny.policymanager.ui.dialogs.SetDeploymentTargetDialog;
import com.bluejungle.destiny.policymanager.ui.dialogs.SetDeploymentTargetDialog.Deploy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.exceptions.CombiningAlgorithm;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author dstarke
 * 
 */
public class PolicyHelpers {
	volatile static boolean msgHasShown = true;
    
    //TODO merge this to com.bluejungle.pf.domain.epicenter.misc.IEffectType
    public static enum EffectTypeEnum {
        ALLOW_ONLY
      , DENY
      , ALLOW
      , UNKNOWN
    }

    //TODO rename this method
	public static EffectTypeEnum getIndexForEffect(IEffectType main, IEffectType otherwise) {
		if (main == EffectType.DENY) {
			return EffectTypeEnum.DENY;
		} else if (main == EffectType.ALLOW) {
			if (otherwise != null && otherwise == EffectType.DENY) {
				return EffectTypeEnum.ALLOW_ONLY;
			} else {
				return EffectTypeEnum.ALLOW;
			}
		}
		return EffectTypeEnum.UNKNOWN;
	}

	public static final Map<EffectTypeEnum, String>  effectTypeToStringMap;
    
	static {
	    effectTypeToStringMap = new HashMap<EffectTypeEnum, String> ();
	    effectTypeToStringMap.put(EffectTypeEnum.DENY, "Deny");
	    effectTypeToStringMap.put(EffectTypeEnum.ALLOW_ONLY, "Allow Only");
	    effectTypeToStringMap.put(EffectTypeEnum.ALLOW, "Allow");
	}

	public static String getStringFromEffectType (EffectTypeEnum effectTypeEnum) {
		return effectTypeToStringMap.get(effectTypeEnum);
	}

	public static void saveEffect(IDPolicy policy, EffectTypeEnum effectTypeEnum) {
		switch (effectTypeEnum) {
		case DENY:
			policy.setMainEffect(EffectType.DENY);
			policy.setOtherwiseEffect(EffectType.ALLOW);
			break;
		case ALLOW_ONLY:
			policy.setMainEffect(EffectType.ALLOW);
			policy.setOtherwiseEffect(EffectType.DENY);
			break;
		case ALLOW:
			policy.setMainEffect(EffectType.ALLOW);
			policy.setOtherwiseEffect(null);
			break;
		}
	}
	
	public static boolean userSessionExpired(PolicyEditorException exception) {
		if (exception.getCause() != null) {
			PolicyServiceFault psf = (PolicyServiceFault)exception.getCause();
			String reason = psf.getCauseMessage();
			if ((reason != null) && (reason.indexOf("not get current loggedInUser") > 0))
				return true;
		}
		return false;
	}
	
	public static void timeOutCheck(PolicyEditorException e){
        if (userSessionExpired(e) && msgHasShown) {
        	msgHasShown = false;
        	MessageDialog.openError(Display.getCurrent().getActiveShell(),
					ApplicationMessages.POLICYSERVERPROXY_ERROR, ApplicationMessages.POLICYSERVERPROXY_LOGIN_MSG);
			shutDownWorkBench();
		}
	}
	
    public static synchronized void shutDownWorkBench(){
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		if (display == null)
			return;
		if (workbench != null && !workbench.isClosing()) {
			display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					ResourceManager.dispose();
					workbench.close();
//					workbench.restart();
				}
			}
			});
		}
    }
	public static void setCombiningAlgorithmByPolicy(IDPolicy policy){
		IEffectType main = policy.getMainEffect();
		IEffectType other = policy.getOtherwiseEffect();
		EffectTypeEnum effectTypeEnum = getIndexForEffect(main, other);
		IPolicyExceptions policyExceptionObject = policy.getPolicyExceptions();
		switch (effectTypeEnum) {
		case DENY:
			policyExceptionObject.setCombiningAlgorithm(CombiningAlgorithm.ALLOW_OVERRIDES);
			break;
		case ALLOW_ONLY:
			policyExceptionObject.setCombiningAlgorithm(CombiningAlgorithm.ALLOW_OVERRIDES);
			break;
		case ALLOW:
			policyExceptionObject.setCombiningAlgorithm(CombiningAlgorithm.DENY_OVERRIDES);
			break;
		}
	}
	
	public static String getPolicyTypeString (String type){
    	if (type.equals("COMMUNICATION_POLICY")) {
            return "Communication Policy";
    	}else if (type.equals("DEVICE_CONTROL_POLICY")){
            return "Device Control Policy";
    	}else if (type.equals("DOCUMENT_POLICY")){
    		return "Document Policy";
    	}else if (type.equals("DYNAMIC_ACCESS_CONTROL_POLICY")) {
            return "Dynamic Access Control Policy";	
        }
        return null;
	}
	
	public static String exceptionTagName (IPolicyReference exceptionRef){	
		String current = exceptionRef.getReferencedName();
		String[] array = current.split("/");
		current = array[array.length - 1];
		return current;
	}
	
    public static List<DODDigest> filterExceptionDependencies(DODDigest dod){
        List<DODDigest> list = new ArrayList<DODDigest>();
        list.add(dod);
        Collection<DODDigest> dependencies = PolicyServerProxy.getAllDependenciesDigest(list);
        List<DODDigest> exceptionDependencies = new ArrayList<DODDigest> ();
        for (DODDigest desc : dependencies) {
        	if(desc.getType().equals("POLICY")){
        		exceptionDependencies.add(desc);
        	}
        }
        return exceptionDependencies;    	
    }
    
    public static Set<DomainObjectDescriptor> filterExceptionDependencies( Set<DomainObjectDescriptor> dsp){
		Collection<DomainObjectDescriptor> dependencies = PolicyServerProxy.getAllDependencies(dsp);
		Set <DomainObjectDescriptor> exceptionDependencies = new HashSet<DomainObjectDescriptor>();
        for (DomainObjectDescriptor desp : dependencies) {
        	if(desp.getType().equals(EntityType.POLICY)){
        		exceptionDependencies.add(desp);
        	}
        }
        return exceptionDependencies;    	
    }
    
    public static IPolicyReference findRefByName(List<IPolicyReference> policyExceptionRef, String name){
    	try{    	
    		for (IPolicyReference ref : policyExceptionRef){
    		String refName = ref.getReferencedName();
    			if(refName.equalsIgnoreCase(name)){
    				return ref;
    			}
    		}
    	}catch (Exception e) {
            LoggingUtil.logError(Activator.ID, "Cannot find exception reference", e);
    	}
    	return null;
    }
    
	public static boolean isSubPolicy(IHasId domainObject){
		boolean hasSubPolicyType ;
		if (!(domainObject instanceof Policy)){
			hasSubPolicyType= false;
		}else{
			hasSubPolicyType = ((Policy) domainObject).hasAttribute(IPolicy.EXCEPTION_ATTRIBUTE);
		}
		return hasSubPolicyType;
	}
	
	public static boolean isDACPolicyType(IHasId domainObject){
		boolean hasDACPolicyType ;
		if (!(domainObject instanceof Policy)){
			hasDACPolicyType= false;
		}else{
			hasDACPolicyType = ((Policy) domainObject).hasAttribute("DYNAMIC_ACCESS_CONTROL_POLICY");
		}
		return hasDACPolicyType;
	}

	public static boolean isSelectedADEnforcer (){
		IHasId current = null;
		Boolean inPolicyAuthorView = PolicyStudioActionFactory.SWITCH_TO_POLICY_MANAGER_ACTION.isEnabled();
		if(inPolicyAuthorView){
			current = (IHasId) GlobalState.getInstance().getCurrentObject();
		}else{
			Set<DomainObjectDescriptor> selectedItems = GlobalState.getInstance().getCurrentSelection();
			Iterator<DomainObjectDescriptor> selectedItemsIterator = selectedItems.iterator();
			DomainObjectDescriptor item = (DomainObjectDescriptor) selectedItemsIterator.next();
			current = (IHasId) PolicyServerProxy.getEntityForDescriptor(item);
		}
		List<IHasId> policyList = new ArrayList<IHasId>();
		IDPolicy policy = (IDPolicy) current;
		policyList.add(current);
		SetDeploymentTargetDialog dlg = new SetDeploymentTargetDialog(Display.getCurrent().getActiveShell(), policyList);
		List<LeafObject> selsectedEnforcer = dlg.getSelectedEnforcersList();
		
		Deploy type ;		
		if (policy.getDeploymentTarget() == null) {
			type = Deploy.AUTO_DEPLOY;
		} else {
			type = Deploy.MANUAL_DEPLOY;
		}
		
		if (type==Deploy.AUTO_DEPLOY) {
			return false;
		}else if ((type==Deploy.MANUAL_DEPLOY)){
			if(selsectedEnforcer.isEmpty()){		
				return false;
			}else {			
				return true;
			}
		}else{
			return false;			
		}
	}
	
	public static boolean isSelectedADEnforcer (IHasId current){
		IDPolicy policy = (IDPolicy) current;
		List<IHasId> policyList = new ArrayList<IHasId>();
		policyList.add(current);
		SetDeploymentTargetDialog dlg = new SetDeploymentTargetDialog(Display.getCurrent().getActiveShell(), policyList);
		List<LeafObject> selsectedEnforcer = dlg.getSelectedEnforcersList();
		Deploy type ;		
		if (policy.getDeploymentTarget() == null) {
			type = Deploy.AUTO_DEPLOY;
		} else {
			type = Deploy.MANUAL_DEPLOY;
		}
		
		if (type==Deploy.AUTO_DEPLOY) {
			return false;
		}else if ((type==Deploy.MANUAL_DEPLOY)){
			if(selsectedEnforcer.isEmpty()){		
				return false;
			}else {			
				return true;
			}
		}else{
			return false;			
		}
	}

    public static Comparator<IHasId> CASE_INSENSITIVE_COMPARATOR = new Comparator<IHasId>() {
        public int compare(IHasId lhs, IHasId rhs) {
        	String lname = null;
        	String rname = null;
        	if(lhs instanceof Policy){
       			Policy p = (Policy)lhs;
       			lname = p.getName();
        	}else if (lhs instanceof PolicyFolder){
       			PolicyFolder f = (PolicyFolder)lhs;
       			lname = f.getName();
        	}
        	if(rhs instanceof Policy){
       			Policy p = (Policy)rhs;
       			rname = p.getName();
        	}else if (rhs instanceof PolicyFolder){
       			PolicyFolder f = (PolicyFolder)rhs;
       			rname = f.getName();
        	}
        	if (lname == null || rname == null){
        		throw new NullPointerException("IHasId name");
        	}
        	return lname.compareToIgnoreCase(rname);
        }
    };
}
