/*
 * Created on Apr 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.ObjectLabelImageProvider;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author fuad
 * @version $Id:
 *          //depot/branch/Destiny_1.0_Final/main/src/client/policymanager/src
 *          /java/main/com/bluejungle/destiny/policymanager/ui/controls/
 *          DependencyControl.java#1 $
 */

public class DependencyControl extends ScrolledComposite {

	public static final String INFO_ICON = "INFO_ICON";
	public static final String WARN_ICON = "WARN_ICON";
	public static final String ERROR_ICON = "ERROR_ICON";

	private boolean isSelectable = false;
	private String label;
	private List<String> names = null;
	private List<String> sectionLabels = new ArrayList<String>();
	private List<Image> sectionIcons = new ArrayList<Image>();
	private List<String> sectionInfo = new ArrayList<String>();
	private List<List<Dependency>> sectionObjects = new ArrayList<List<Dependency>>();
	private List<Button> checkBoxList = new ArrayList<Button>();
	private List<CLabel> labelList = new ArrayList<CLabel>();
	private Composite mainComposite;

	private MouseListener labelClickListener = new MouseAdapter() {
		@Override
		public void mouseDown(MouseEvent e) {
			if (e.button == 1) {
				int index = labelList.indexOf(e.getSource());
				if (index >= 0) {
					Button checkBox = (Button) checkBoxList.get(index);
					checkBox.setSelection(!checkBox.getSelection());
				}
			}
		}
	};

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public DependencyControl(Composite parent, int style, boolean isSelectable) {
		super(parent, style | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
		this.isSelectable = isSelectable;
	}

	/**
	 * Returns the label.
	 * 
	 * @return the label.
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Sets the label
	 * 
	 * @param label
	 *            The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets the names
	 * 
	 * @param names
	 *            The names to set.
	 */
	public void setNames(List<String> names) {
		this.names = names;
	}

	/**
	 * adds a section to the control.
	 * 
	 * @param label
	 *            label for section
	 * @param icon
	 *            icon to show for section
	 * @param info
	 *            info string to display for this section
	 * @param objects
	 *            object list to be displayed in this section
	 */
	public void addSection(String label, Image icon, String info,
			List<Dependency> objects) {
		sectionLabels.add(label);
		sectionIcons.add(icon);
		sectionInfo.add(info);
		sectionObjects.add(objects);
	}

	/**
     * 
     */
	public void initialize() {
		getVerticalBar().setIncrement(10);
		getVerticalBar().setPageIncrement(100);
		getHorizontalBar().setIncrement(10);
		getHorizontalBar().setPageIncrement(100);
		setExpandHorizontal(true);
		setExpandVertical(true);

		mainComposite = new Composite(this, SWT.NONE);
		mainComposite.setBackground(getBackground());
		setContent(mainComposite);
		GridLayout gridLayout = new GridLayout();
		mainComposite.setLayout(gridLayout);

		if (label != null) {
			addHeading();
		}

		for (int i = 0; i < sectionLabels.size(); i++) {
			Composite section = new Composite(mainComposite, SWT.NONE);
			section.setBackground(getBackground());
			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			section.setLayoutData(gridData);

			FormLayout formLayout = new FormLayout();
			formLayout.marginHeight = 5;
			formLayout.marginWidth = 5;
			formLayout.spacing = 0;
			section.setLayout(formLayout);

			CLabel label = new CLabel(section, SWT.NONE);
			label.setBackground(getBackground());
			label.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
			label.setText((String) sectionLabels.get(i));
			label.setImage((Image) sectionIcons.get(i));
			FormData formData = new FormData();
			formData.left = new FormAttachment(0);
			formData.top = new FormAttachment(0);
			label.setLayoutData(formData);

			Composite sectionLine = new Composite(section, SWT.NONE);
			sectionLine.setBackground(getDisplay().getSystemColor(
					SWT.COLOR_GRAY));
			formData = new FormData();
			formData.height = 1;
			formData.left = new FormAttachment(label, 0, SWT.RIGHT);
			formData.top = new FormAttachment(label, 0, SWT.CENTER);
			formData.right = new FormAttachment(100);
			sectionLine.setLayoutData(formData);

			List<Dependency> objectList = sectionObjects.get(i);
			if (objectList.size() > 0) {
				String infoString = (String) sectionInfo.get(i);
				if (infoString != null) {
					Label info = new Label(section, SWT.WRAP);
					info.setBackground(getBackground());
					info.setForeground(getDisplay().getSystemColor(
							SWT.COLOR_DARK_GRAY));
					info.setText(infoString);
					formData = new FormData();
					formData.width = 200;
					formData.left = new FormAttachment(100, -220);
					formData.top = new FormAttachment(label, 0, SWT.BOTTOM);
					// formData.right = new FormAttachment (100, 0);
					formData.bottom = new FormAttachment(100, 0);
					info.setLayoutData(formData);
				}

				Control previousControl = label;
				for (int j = 0; j < objectList.size(); j++) {
					Dependency nextDependency = (Dependency) objectList.get(j);
					DomainObjectDescriptor desc = nextDependency
							.getDependencyObject();
					formData = new FormData();
					formData.left = new FormAttachment(0, 15);
					if (isSelectable) {
						Button checkBox = new Button(section, SWT.CHECK);
						checkBox.setBackground(getBackground());
						checkBox.setData(desc);
						checkBox.setSelection(nextDependency.isChecked());
						checkBox.setEnabled(nextDependency.isSelectable());

						checkBoxList.add(checkBox);
						formData.top = new FormAttachment(previousControl, 2,
								SWT.BOTTOM);
						checkBox.setLayoutData(formData);

						// Setup FormData for label to right of checkbox
						formData = new FormData();
						formData.left = new FormAttachment(checkBox, 0,
								SWT.RIGHT);
					}
					CLabel objectNameLabel = new CLabel(section, SWT.NONE);
					labelList.add(objectNameLabel);
					if ((isSelectable) && (nextDependency.isSelectable())) {
						objectNameLabel.addMouseListener(labelClickListener);
					}
					objectNameLabel.setBackground(getBackground());
					objectNameLabel.setText(DomainObjectHelper
							.getDisplayName(desc));
					objectNameLabel.setImage(ObjectLabelImageProvider
							.getImage(desc));
					formData.top = new FormAttachment(previousControl, 0,
							SWT.BOTTOM);
					formData.right = new FormAttachment(65);
					objectNameLabel.setLayoutData(formData);
					previousControl = objectNameLabel;
				}
			} else {
				Label noneLabel = new Label(section, SWT.NONE);
				noneLabel.setBackground(getBackground());
				noneLabel.setForeground(getDisplay().getSystemColor(
						SWT.COLOR_DARK_GRAY));
				noneLabel.setText("none");
				formData = new FormData();
				formData.left = new FormAttachment(0, 15);
				formData.top = new FormAttachment(label, 0, SWT.BOTTOM);
				noneLabel.setLayoutData(formData);
			}

		}

		setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
     * 
     */
	private void addHeading() {
		Composite header = new Composite(mainComposite, SWT.NONE);
		header.setBackground(getBackground());
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		formLayout.spacing = 5;
		header.setLayout(formLayout);

		Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setBackground(getBackground());
		headerLabel.setForeground(getDisplay().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		headerLabel.setText(label);

		FormData formData = new FormData();
		formData.height = 20;
		formData.left = new FormAttachment(0);
		formData.top = new FormAttachment(0);
		headerLabel.setLayoutData(formData);

		formData = new FormData();
		formData.left = new FormAttachment(headerLabel, 0);
		formData.top = new FormAttachment(0);

		if (names.size() == 1) {
			Label objectNames = new Label(header, SWT.NONE);
			objectNames.setBackground(getBackground());
			objectNames.setText((String) names.get(0));
			objectNames.setLayoutData(formData);
		} else {
			org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(
					header, SWT.SINGLE);
			list.setBackground(getBackground());
			for (int i = 0; i < names.size(); i++) {
				list.add((String) names.get(i));
			}
			list.setLayoutData(formData);
		}
	}

	/**
	 * @return a list of selected objects
	 */
	public List<DomainObjectDescriptor> getSelection() {
		List<DomainObjectDescriptor> selection = new ArrayList<DomainObjectDescriptor>();

		for (int i = 0; i < checkBoxList.size(); i++) {
			Button checkBox = (Button) checkBoxList.get(i);
			if (checkBox.getSelection()) {
				selection.add((DomainObjectDescriptor) checkBox.getData());
			}
		}

		return selection;
	}

	public static class Dependency {

		private boolean isSelectable;
		private boolean isChecked;
		private DomainObjectDescriptor dependencyObject;

		/**
		 * Create an instance of Dependency
		 * 
		 * @param dependencyObject
		 * @param isSelectable
		 * @param isChecked
		 */
		public Dependency(DomainObjectDescriptor dependencyObject,
				boolean isSelectable, boolean isChecked) {
			if (dependencyObject == null) {
				throw new NullPointerException(
						"dependencyObject cannot be null.");
			}

			this.dependencyObject = dependencyObject;
			this.isSelectable = isSelectable;
			this.isChecked = isChecked;
		}

		/**
		 * Retrieve the dependencyObject.
		 * 
		 * @return the dependencyObject.
		 */
		private DomainObjectDescriptor getDependencyObject() {
			return dependencyObject;
		}

		/**
		 * Retrieve the isChecked.
		 * 
		 * @return the isChecked.
		 */
		private boolean isChecked() {
			return isChecked;
		}

		/**
		 * Retrieve the isSelectable.
		 * 
		 * @return the isSelectable.
		 */
		private boolean isSelectable() {
			return isSelectable;
		}
	}
}