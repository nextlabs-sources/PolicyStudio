/*
 * Created on Jul 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs,
 * Inc., San Mateo CA, Ownership remains with NextLabs, Inc., All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.event.IEventManager;
import com.bluejungle.destiny.policymanager.event.PolicyOrComponentModifiedEvent;
import com.bluejungle.destiny.policymanager.event.defaultimpl.EventManagerImpl;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.deployment.AgentAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/main/com.nextlabs.policystudio/src/com/bluejungle
 *          /destiny/policymanager/ui/dialogs/DeployWizard.java#3 $
 */

public class DeployWizard extends Wizard {

	private static final IEventManager EVENT_MANAGER;
	static {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		EVENT_MANAGER = componentManager.getComponent(EventManagerImpl.COMPONENT_INFO);
	}

	private static IExpression PORTAL_TYPE_CONST = Constant.build(AgentTypeEnumType.PORTAL.getName());
	private static IExpression DESKTOP_TYPE_CONST = Constant.build(AgentTypeEnumType.DESKTOP.getName());
	private static IExpression FILE_SERVER_TYPE_CONST = Constant.build(AgentTypeEnumType.FILE_SERVER.getName());
	private static IExpression ACTIVE_DIRECTORY_CONST = Constant.build(AgentTypeEnumType.ACTIVE_DIRECTORY.getName());

	private enum Deploy {
		AUTO_DEPLOY, MANUAL_DEPLOY
	};

	private enum Type {
		FILE_SERVER(0), PORTAL(2), WIN_DESKTOP(1), ACTIVE_DIRECTORY(3);

		private int type;

		Type(int type) {
			this.type = type;
		}

		int getType() {
			return type;
		}
	}

	private List<DomainObjectDescriptor> descriptors;
	private ScheduleDeploymentPage dependencyPage;
	private DeploymentTargetPage deploymentTargetPage;
	private boolean hasPolicy = false;

	public DeployWizard(List<DomainObjectDescriptor> descriptors) {
		super();

		this.descriptors = descriptors;

		dependencyPage = new ScheduleDeploymentPage(descriptors);

		addPage(dependencyPage);

		for (DomainObjectDescriptor descriptor : descriptors) {
			if (descriptor.getType() == EntityType.POLICY) {
				hasPolicy = true;
				break;
			}
		}
		if (hasPolicy) {
			deploymentTargetPage = new DeploymentTargetPage();
			addPage(deploymentTargetPage);
		}

		setWindowTitle(DialogMessages.SCHEDULEDEPLOYMENTDIALOG_DEPLOY);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		getShell().setImage(ImageBundle.POLICYSTUDIO_IMG);
	}

	public boolean checkDepencies() {
		return dependencyPage.checkDependencies();
	}

	@Override
	public boolean performFinish() {
		if (hasPolicy) {
			for (DomainObjectDescriptor descriptor : descriptors) {
				if (descriptor.getType() == EntityType.POLICY) {
					IDPolicy policy = (IDPolicy) PolicyServerProxy.getEntityForDescriptor(descriptor);
					setTarget(policy);
				}
			}
		}

		List<DomainObjectDescriptor> selectedObjects = dependencyPage.getSelectedObjects();
		Date deployTime = dependencyPage.getDeploymentTime();

		PolicyServerProxy.deployObjects(selectedObjects, deployTime);

		Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>(selectedObjects.size());
		for (DomainObjectDescriptor nextObjectDeployed : selectedObjects) {
			PolicyOrComponentModifiedEvent nextEvent = new PolicyOrComponentModifiedEvent(nextObjectDeployed);
			eventsToFire.add(nextEvent);
		}

		EVENT_MANAGER.fireEvent(eventsToFire);

		return true;
	}

	@SuppressWarnings("unchecked")
	private void setTarget(IDPolicy policy) {
		Deploy orig = policy.getDeploymentTarget() != null ? Deploy.MANUAL_DEPLOY: Deploy.AUTO_DEPLOY;
		List<IDPolicy> modifiedPolicies = new ArrayList<IDPolicy>();
		int type = deploymentTargetPage.getDeploymentType();
		Deploy deploymentType;
		if (type == 0) {
			deploymentType = Deploy.AUTO_DEPLOY;
		} else {
			deploymentType = Deploy.MANUAL_DEPLOY;
		}

		List<LeafObject> selectedFileServers, selectedPortals, selectedWinDesktops, selectedActiveDirectories;
		if (deploymentType == Deploy.AUTO_DEPLOY && deploymentType != orig) {
			if (policy.getDeploymentTarget() != null) {
				policy.setDeploymentTarget(null);
				modifiedPolicies.add(policy);
			}
		} else if (deploymentType == Deploy.MANUAL_DEPLOY) {
			CompositePredicate deploymentTarget = (CompositePredicate) policy.getDeploymentTarget();
			if (deploymentTarget == null) {
				deploymentTarget = createDeploymentTargetPredicate();
				policy.setDeploymentTarget(deploymentTarget);
			}
			selectedFileServers = new ArrayList<LeafObject>();
			selectedPortals = new ArrayList<LeafObject>();
			selectedWinDesktops = new ArrayList<LeafObject>();
			selectedActiveDirectories = new ArrayList<LeafObject>();
			
			List<LeafObject> selectedEnforcers = deploymentTargetPage.getSelectedEnforcers();
			for (LeafObject object : selectedEnforcers) {
				if (object.getType() == LeafObjectType.FILE_SERVER_AGENT) {
					selectedFileServers.add(object);
				} else if (object.getType() == LeafObjectType.PORTAL_AGENT) {
					selectedPortals.add(object);
				} else if (object.getType() == LeafObjectType.DESKTOP_AGENT) {
					selectedWinDesktops.add(object);
				} else if (object.getType() == LeafObjectType.ACTIVE_DIRECTORY_AGENT) {
					selectedActiveDirectories.add(object);
				}
			}
			updatePredicates(deploymentTarget, Type.FILE_SERVER,selectedFileServers);
			updatePredicates(deploymentTarget, Type.PORTAL, selectedPortals);
			updatePredicates(deploymentTarget, Type.WIN_DESKTOP,selectedWinDesktops);
			updatePredicates(deploymentTarget, Type.ACTIVE_DIRECTORY, selectedActiveDirectories);
			modifiedPolicies.add(policy);
		}

		// save all policies
		if (modifiedPolicies.size() != 0) {
			PolicyServerProxy.saveEntities(modifiedPolicies);

			Set<PolicyOrComponentModifiedEvent> eventsToFire = new HashSet<PolicyOrComponentModifiedEvent>();
			Iterator modifiedPoliciesIterator = modifiedPolicies.iterator();
			while (modifiedPoliciesIterator.hasNext()) {
				Object nextModifiedPolicy = modifiedPoliciesIterator.next();
				PolicyOrComponentModifiedEvent objectModifiedEvent = new PolicyOrComponentModifiedEvent((IHasId) nextModifiedPolicy);
				eventsToFire.add(objectModifiedEvent);
			}

			EVENT_MANAGER.fireEvent(eventsToFire);
		}
	}

	private CompositePredicate createDeploymentTargetPredicate() {
		CompositePredicate ret = new CompositePredicate(BooleanOp.OR,new ArrayList<IPredicate>());

		// add fileserver predicate
		CompositePredicate fileServerPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(fileServerPredicate);
		CompositePredicate fileServerList = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
		IRelation fileServerRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, FILE_SERVER_TYPE_CONST);
		fileServerPredicate.addPredicate(fileServerList);
		fileServerPredicate.addPredicate(fileServerRelation);
		fileServerList.addPredicate(PredicateConstants.FALSE);
		fileServerList.addPredicate(PredicateConstants.FALSE);

		// add desktop predicate
		CompositePredicate desktopPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(desktopPredicate);
		CompositePredicate desktopList = new CompositePredicate(BooleanOp.OR,new ArrayList<IPredicate>());
		IRelation desktopRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, DESKTOP_TYPE_CONST);
		desktopPredicate.addPredicate(desktopList);
		desktopPredicate.addPredicate(desktopRelation);
		desktopList.addPredicate(PredicateConstants.FALSE);
		desktopList.addPredicate(PredicateConstants.FALSE);

		// add portal predicate
		CompositePredicate ListPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(ListPredicate);
		CompositePredicate ListList = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
		IRelation ListRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, PORTAL_TYPE_CONST);
		ListPredicate.addPredicate(ListList);
		ListPredicate.addPredicate(ListRelation);
		ListList.addPredicate(PredicateConstants.FALSE);
		ListList.addPredicate(PredicateConstants.FALSE);

		// add Active Directory predicate
		CompositePredicate ActiveDPredicate = new CompositePredicate(BooleanOp.AND, new ArrayList<IPredicate>());
		ret.addPredicate(ActiveDPredicate);
		CompositePredicate ActiveDList = new CompositePredicate(BooleanOp.OR,	new ArrayList<IPredicate>());
		IRelation ActiveDRelation = AgentAttribute.TYPE.buildRelation(RelationOp.EQUALS, ACTIVE_DIRECTORY_CONST);
		ActiveDPredicate.addPredicate(ActiveDList);
		ActiveDPredicate.addPredicate(ActiveDRelation);
		ActiveDList.addPredicate(PredicateConstants.FALSE);
		ActiveDList.addPredicate(PredicateConstants.FALSE);
		
		return ret;
	}

	private void updatePredicates(CompositePredicate deploymentTarget,Type type, List<LeafObject> leafObjects) {
		CompositePredicate newPred = new CompositePredicate(BooleanOp.OR,PredicateConstants.FALSE);
		if (!leafObjects.isEmpty()) {
			for (LeafObject leaf : leafObjects) {
				newPred.addPredicate(AgentAttribute.ID.buildRelation(RelationOp.EQUALS, Constant.build(leaf.getId().longValue())));
			}
		} else {
			newPred.addPredicate(PredicateConstants.FALSE);
		}
		CompositePredicate compositeAnd = (CompositePredicate) ((CompositePredicate) deploymentTarget.predicateAt(type.getType()));
		compositeAnd.removePredicate(0);
		compositeAnd.insertElement(newPred, 0);
	}
}
