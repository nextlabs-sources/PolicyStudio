package com.bluejungle.destiny.policymanager.model;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.destiny.services.PolicyEditorException;

public class EntityInformation {
	public static boolean loadingComplete = false;
	public static int total;
	public static int current;

	private String fullName = "";
	private String displayName = "";
	private int version;
	private Date submittedTime;
	private String formatedTime = "";
	private String submittedBy = "";
	private String status = "";
	private boolean hasChildren;

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String fullName) {
		this.displayName = fullName;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getSubmittedTime() {
		return submittedTime;
	}

	public void setSubmittedTime(Date submittedTime) {
		this.submittedTime = submittedTime;
	}

	public String getSubmittedBy() {
		return submittedBy;
	}

	public void setSubmittedBy(String submittedBy) {
		this.submittedBy = submittedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFormatedTime() {
		return formatedTime;
	}

	public void setFormatedTime(String formatedTime) {
		this.formatedTime = formatedTime;
	}

	public static String getDisplayName(DODDigest descriptor) {
		String fullName = descriptor.getName();
		int index = fullName.lastIndexOf(PQLParser.SEPARATOR);
		if (index < 0) {
			return fullName;
		} else {
			return fullName.substring(index + 1, fullName.length());
		}
	}

	public static String getDisplayName(DomainObjectDescriptor descriptor) {
		String fullName = descriptor.getName();
		int index = fullName.lastIndexOf(PQLParser.SEPARATOR);
		if (index < 0) {
			return fullName;
		} else {
			return fullName.substring(index + 1, fullName.length());
		}
	}

	public static String getStatus(DODDigest digest) {
		DevelopmentStatus status = digest.getDevStatus();
		DomainObjectUsage usage = digest.getUsageStatus();
		int version = digest.getVersion();
		String statusKey = DomainObjectHelper.getDeploymentStatusKey(version,
				status, usage);
		String statusText = DomainObjectHelper.getStatusText(statusKey);
		String deploymentText = DomainObjectHelper.getDeploymentText(statusKey);
		String result;
		if (deploymentText != null && deploymentText.length() != 0) {
			result = statusText + " (" + deploymentText + ")";
		} else {
			result = statusText;
		}
		return result;
	}

	public static String getActivatedBy(DODDigest descriptor) {
		String owner = descriptor.getOwnerName();
		if (owner != null) {
			return owner;
		}
		return DialogMessages.OBJECTPROPERTIESDIALOG_UNKNOWN_OR_DELETED;
	}

	public static int getVerion(DomainObjectDescriptor descriptor) {
		Collection<DeploymentHistory> records = PolicyServerProxy.getDeploymentRecords(descriptor);
		if (records != null) {
			return records.size();
		}
		return 0;
	}

	public static String getActivationTime(DomainObjectDescriptor descriptor) {
		Collection<DeploymentHistory> records = PolicyServerProxy.getDeploymentRecords(descriptor);
		if (records == null || records.isEmpty()) {
            return null;
        }
		Date activeFrom = TimeRelationHelper.getLatestActiveFrom(records);
		
		String time;
        if (activeFrom != null) {
            Format formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa");
            time = formatter.format(activeFrom);
        } else {
            time = "";
        }
        return time;
	}

	public static String getStatus(DomainObjectDescriptor descriptor) {
		String result = "";
		List<DomainObjectUsage> usages = new ArrayList<DomainObjectUsage>();
		try {
			usages = PolicyServerProxy.client.getUsageList(Collections.singletonList(descriptor));
		} catch (PolicyEditorException e) {
			e.printStackTrace();
			return result;
		}
		DomainObjectUsage usage = usages.get(0);
		if (usage == null) {
			try {
				usage = PolicyServerProxy.getUsage(descriptor);
			} catch (PolicyEditorException e) {
				e.printStackTrace();
				return result;
			}
		}
		String statusKey = DomainObjectHelper.getDeploymentStatusKey(
				descriptor, usage);
		String statusText = DomainObjectHelper.getStatusText(statusKey);
		String deploymentText = DomainObjectHelper.getDeploymentText(statusKey);
		if (deploymentText != null && deploymentText.length() != 0) {
			result = statusText + " (" + deploymentText + ")";
		} else {
			result = statusText;
		}
		return result;
	}

	public static String getActivatedBy(DomainObjectDescriptor descriptor) {
		LeafObject ownerLeaf = EntityInfoProvider.getLeafObjectByID(descriptor
				.getOwner(), LeafObjectType.APPUSER);
		if (ownerLeaf == null) {
			return DialogMessages.OBJECTPROPERTIESDIALOG_UNKNOWN_OR_DELETED;
		}
		return ownerLeaf.getName();
	}
}
