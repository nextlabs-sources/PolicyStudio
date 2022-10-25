/*
 * Created on May 9, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ui.ApplicationMessages;
import com.bluejungle.destiny.policymanager.ui.ColorBundle;
import com.bluejungle.destiny.policymanager.ui.DomainObjectHelper;
import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.ui.GlobalState;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.ObjectProperty;
import com.bluejungle.destiny.policymanager.ui.PolicyHelpers;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.ui.PropertyUndoElement;
import com.bluejungle.destiny.policymanager.ui.GlobalState.SaveCause;
import com.bluejungle.destiny.policymanager.ui.dialogs.DialogMessages;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;

/**
 * @author dstarke
 * 
 */
public abstract class EditorPanel extends Composite implements IEditorPanel {

	public static final int SPACING = 5;
	public static final int SIDE_SPACING = 30;
	public static final int TOP_SPACKING = 20;


	// --Internal State-------
	protected IHasId domainObject;
	private boolean editable = true;
	private boolean isInitialized = false;
	private boolean ignoreRelayout = false;

	// --Widgets--------------
	protected Text descriptionText = null;
	protected Composite mainComposite = null;
	protected Composite headingComposite = null;
	protected Composite leftEditorComposite = null;
	protected Composite rightEditorComposite = null;	
	protected Composite footerComposite = null;
	protected ScrolledComposite scrolledComposite = null;
	protected Label objectTypeLabel = null;
	protected Label descriptionLabel = null;
	protected String displayName;
	protected Button addButton, deleteButton;
	protected Text nameText, valueText;
	protected Collection<IPair<String, String>> policyTags;
	protected TableViewer tableViewer;
	private SashForm vSash = null;
	protected int tagsCount;
	protected int tagNo=1;
	
	// --Positioning Information---
	private static final Point SIZE_DESCRIPTION_TEXT = new Point(300, 120);

	// --Contstructor----------
	public EditorPanel(Composite parent, int style, IHasId domainObject) {
		super(parent, style);
		this.domainObject = domainObject;
		setBackground(ResourceManager.getColor("EDITOR_BACKGROUD", Activator
				.getDefault().getPluginPreferences().getString(
						"EDITOR_BACKGROUD")));
	}

	public Composite getMainComposite() {
		return mainComposite;
	}
	
	public Composite getLeftEditorComposite() {
		return leftEditorComposite;
	}
	   
	public Composite getRightEditorComposite() {
		return rightEditorComposite;
	}
	
	public ScrolledComposite getScrolledComposite(){
		return scrolledComposite;
	}

	// --Initialization--------
	public void initialize() {
		initializeFrame();
		initializeHeading();
		initializeContents();
		initializeFooter();
		isInitialized = true;
	}

	protected void initializeFrame() {
		setLayout(new FillLayout());

		mainComposite = new Composite(this, SWT.NONE);
		mainComposite.setBackground(getBackground());
		GridLayout layout = new GridLayout(2, false);
		mainComposite.setLayout(layout);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		headingComposite = new Composite(mainComposite, SWT.BORDER);
		headingComposite.setBackground(getBackground());
		headingComposite.setLayoutData(data);
		
		vSash = new SashForm(mainComposite, SWT.HORIZONTAL);
		data = new GridData(GridData.FILL_BOTH);
		vSash.setLayoutData(data);
		layout = new GridLayout();
		vSash.setLayout(layout);
		
		scrolledComposite = new ScrolledComposite(vSash, SWT.V_SCROLL| SWT.H_SCROLL);
		scrolledComposite.getVerticalBar().setIncrement(10);
		scrolledComposite.getVerticalBar().setPageIncrement(100);
		scrolledComposite.getHorizontalBar().setIncrement(10);
		scrolledComposite.getHorizontalBar().setPageIncrement(100);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);	
		data = new GridData(GridData.FILL_BOTH );
		scrolledComposite.setLayoutData(data);
		leftEditorComposite = new Composite(scrolledComposite, SWT.BORDER);
		leftEditorComposite.setBackground(getBackground());
		scrolledComposite.setContent(leftEditorComposite);
		data = new GridData(GridData.FILL_BOTH);
		rightEditorComposite = new Composite(vSash, SWT.BORDER);
		rightEditorComposite.setBackground(getBackground());
		rightEditorComposite.setLayoutData(data);
	
		vSash.setWeights(new int[] { 2, 1});
	}

	public static Composite addSectionComposite(Composite parent, Color color) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// Composite ret = new Composite(mainComposite, SWT.NONE);
		// ret.setBackground(getBackground());
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setBackground(color);
		ret.setLayoutData(data);
		return ret;
	}
	
	public static Composite addRightEditorSectionComposite(Composite parent, Color color) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth  = 0;
		parent.setLayout(gridLayout);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// Composite ret = new Composite(mainComposite, SWT.NONE);
		// ret.setBackground(getBackground());
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setBackground(color);
		ret.setLayoutData(data);
		return ret;
	}
	
	public static Composite addLeftEditorSectionComposite(Composite parent, Color color) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth  = 0;
		parent.setLayout(gridLayout);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// Composite ret = new Composite(mainComposite, SWT.NONE);
		// ret.setBackground(getBackground());
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setBackground(color);
		ret.setLayoutData(data);
		return ret;
	}

	protected void initializeHeading() {
		
		FormLayout headingLayout = new FormLayout();

		headingComposite.setLayout(headingLayout);

		Image image = ImageBundle.POLICY_LOGO;	
		Label logoLable = new Label (headingComposite, SWT.NONE);
		logoLable.setImage(image);
		FormData data = new FormData();
		data.height = 80;
		data.width = 70;
		data.top = new FormAttachment(0,SPACING);
		data.left = new FormAttachment(0,SPACING);
		logoLable.setLayoutData(data);
		
		String type = getDisplayName();
		Label policyTypelabel = null;
		if (type != null) {
			policyTypelabel = new Label(headingComposite, SWT.NONE);
			policyTypelabel.setBackground(getBackground());
			policyTypelabel.setText(type);
			policyTypelabel.setFont(FontBundle.ARIAL_18_NORMAL);
			policyTypelabel.setAlignment(SWT.LEFT);

			data = new FormData();
			data.left = new FormAttachment(logoLable,SPACING);
			data.right = new FormAttachment(100, -SPACING);
			data.top = new FormAttachment(20);
			policyTypelabel.setLayoutData(data);
		}
		
		
		String policyName = getObjectName();
		if (policyName != null) {
			Label policyNamelabel = new Label(headingComposite, SWT.NONE);
			policyNamelabel.setBackground(getBackground());
			policyNamelabel.setText(policyName);
			policyNamelabel.setFont(FontBundle.ARIAL_9_NORMAL);
			policyNamelabel.setAlignment(SWT.LEFT);

			data = new FormData();
			data.left = new FormAttachment(logoLable,SPACING);
			data.right = new FormAttachment(100, -SPACING);
			data.top = new FormAttachment(policyTypelabel, SPACING);
			policyNamelabel.setLayoutData(data);
		}
	}

	protected void initializeFooter() {
		footerComposite = addRightEditorSectionComposite(rightEditorComposite, getBackground());
		FormLayout footerLayout = new FormLayout();
		footerComposite.setLayout(footerLayout);

		//Description---------------------------------------------------------------------------------
		Composite descriptionLabel = initializeSectionHeading(footerComposite,
				EditorMessages.EDITORPANEL_DESCRIPTION);
		FormData data = new FormData();
		data.left = new FormAttachment(0, SPACING);
		data.right = new FormAttachment(100, -SPACING);
		data.top = new FormAttachment(0, EditorPanel.TOP_SPACKING);
		descriptionLabel.setLayoutData(data);

		createDescriptionText();

		data = new FormData();
		data.left = new FormAttachment(0,SPACING);
		data.top = new FormAttachment(descriptionLabel, SPACING);
		data.right = new FormAttachment(100, -SPACING);
		// data.width = SIZE_DESCRIPTION_TEXT.x;
		data.height = 125;
		descriptionText.setLayoutData(data);
		
		//Tag------------------------------------------------------------------------------------------
		if((DomainObjectHelper.getObjectType(domainObject)).equalsIgnoreCase("policy")){
			Composite tagLabel = initializeSectionHeading(footerComposite, EditorMessages.EDITORPANEL_TAG);
			data = new FormData();
			data.left = new FormAttachment(0, SPACING);
			data.right = new FormAttachment(100, -SPACING);
			data.top = new FormAttachment(descriptionText, TOP_SPACKING);
			tagLabel.setLayoutData(data);

			Composite temp = new Composite (footerComposite, SWT.NONE);
			temp.setBackground(getBackground());
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns =3;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			temp.setLayout(gridLayout);
			
			Label nameLabel = new Label(temp, SWT.NULL);
			nameLabel.setText("Name: ");
			nameLabel.setBackground(getBackground());
			GridData  grid = new GridData (GridData.VERTICAL_ALIGN_CENTER);
			grid.widthHint = 45;
			nameLabel.setLayoutData(grid);
			nameText = new Text(temp, SWT.SINGLE | SWT.BORDER);
			grid = new GridData (GridData.FILL_BOTH);
			grid.grabExcessHorizontalSpace = true;
			grid.widthHint = 100;
			grid.heightHint=18;
			nameText.setLayoutData(grid);
			nameText.setTextLimit(64);
			nameText.setEnabled(isEditable());
			new Label(temp, SWT.NULL);
			
			Label valueLabel = new Label(temp, SWT.NULL);
			valueLabel.setText("Value: ");
			valueLabel.setBackground(getBackground());
			grid = new GridData (GridData.VERTICAL_ALIGN_CENTER);
			grid.widthHint = 45; 
			valueLabel.setLayoutData(grid);
			valueText = new Text(temp, SWT.SINGLE | SWT.BORDER);
			valueText.setTextLimit(64);
			valueText.setEnabled(isEditable());
			grid = new GridData (GridData.FILL_BOTH);
			grid.grabExcessHorizontalSpace = true;
			grid.widthHint = 100;
			grid.heightHint=18;
			valueText.setLayoutData(grid);
			
			addButton = new Button(temp, SWT.PUSH);
			addButton.setText("Add");
			addButton.setEnabled(isEditable());
			grid = new GridData (GridData.FILL_BOTH);
			grid.grabExcessHorizontalSpace = true;
			addButton.setLayoutData(grid);
			    
			policyTags = ((Policy)domainObject).getTags();
			tagsCount = policyTags.size();
			
			final TreeViewer tagTreeViewer = new TreeViewer(temp, SWT.BORDER| SWT.FULL_SELECTION | SWT.MULTI);
			tagTreeViewer.setContentProvider(new TagTreeContentProvider());
			tagTreeViewer.setLabelProvider(new TagTreeLabelProvider());

	        final Tree tagTree = tagTreeViewer.getTree();
	        tagTree.setHeaderVisible(true);
	        tagTree.setLinesVisible(true);
	        grid = new GridData(GridData.FILL_BOTH);
	        grid.horizontalSpan = 3;
	        grid.grabExcessHorizontalSpace = true;
	        grid.heightHint = 125;
	        tagTree.setLayoutData(grid);

		    TreeColumn noColumn = new TreeColumn(tagTree, SWT.LEFT);
		    noColumn.setWidth(35);
		    noColumn.setText("No.");
		    TreeColumn nameColumn = new TreeColumn(tagTree, SWT.LEFT);
		    nameColumn.setWidth(150);
		    nameColumn.setText("Name");
		    TreeColumn valueColumn = new TreeColumn(tagTree, SWT.LEFT);
		    valueColumn.setWidth(150);
		    valueColumn.setText("Value");
		    
		    new Label(temp, SWT.NULL);
	    	new Label(temp, SWT.NULL);
	    	
	    	deleteButton = new Button(temp, SWT.PUSH);
	    	deleteButton.setText("Delete");
	    	deleteButton.setEnabled(false);
			grid = new GridData (GridData.FILL_BOTH);
			grid.grabExcessHorizontalSpace = true;
			deleteButton.setLayoutData(grid);

		    tagTree.addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event e) {
		    		TreeItem[] selection = tagTree.getSelection();
		    		deleteButton.setEnabled(false);
		    		if(selection.length!=0){
		    			deleteButton.setEnabled(true);
	    			}
		        }
		      });
		    
			deleteButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					TreeItem[] selection = tagTree.getSelection();

			        for (int i = 0; i < selection.length; i++){
			        	String name = selection[i].getText(1);
			        	String value = selection[i].getText(2);
						removeSelections(name, value);
					}
			        tagsCount = policyTags.size();
			        tagNo = 1;
			        tagTreeViewer.refresh();
					deleteButton.setEnabled(false);
			    }
			});
		    
		    addButton.addSelectionListener(new SelectionAdapter() {
				@Override	
				public void widgetSelected(SelectionEvent e) {
					String name = nameText.getText();
					String value = valueText.getText();
					if(name != null && !name.isEmpty() && !name.trim().isEmpty()
							&& value != null && !value.isEmpty() && !value.trim().isEmpty()){
						if(isNewTag(name,value)){
							TreeItem item = new TreeItem(tagTree, SWT.NONE);
						    item.setText(new String[] {String.valueOf(tagsCount+1),nameText.getText(),valueText.getText()});
							((Policy)domainObject).addTag(name, value);
							tagsCount = policyTags.size();
							name="";
							value="";
							nameText.setText("");
							valueText.setText("");
						}else{
							MessageDialog.openError(getDisplay().getActiveShell(),
									ApplicationMessages.TAG_ERROR,
									ApplicationMessages.TAG_ERROR_DUPLICATE);
							return ;
						}
					}else{
						MessageDialog.openError(getDisplay().getActiveShell(),
								ApplicationMessages.TAG_ERROR,
								ApplicationMessages.TAG_ERROR_EMPTY);
						return ;
					}
				   }
				});
			tagTreeViewer.setInput(policyTags);
		    data = new FormData();
			data.top = new FormAttachment(tagLabel, SPACING);
			data.left= new FormAttachment(0, 10);
			data.right = new FormAttachment(100, -SPACING);
			temp.setLayoutData(data);
		}

	}
	private class TagTreeContentProvider implements ITreeContentProvider {

        public Object[] getElements(Object parent) {
            if (policyTags != null) {
                return policyTags.toArray();
            } else {
                return new Object[0];
            }
        }
        public Object getParent(Object element) {
        	return null;
        }
        
        public Object[] getChildren(Object element) {
        	return null;
        }

        public boolean hasChildren(Object element) {
            return false;
        }
        
        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
	
	private class TagTreeLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object obj, int index) {
			
			if(!(obj instanceof Pair)) return null;
			Pair policyTag = (Pair) obj;
			switch (index) {
			case 0: 
				tagNo++;
				return (String)String.valueOf(tagNo-1);

			case 1:
				return (String) policyTag.first();
			case 2:
				return (String) policyTag.second();
			}
			return null;
		}
	}
	
	private boolean isNewTag(String name, String value){
		return !policyTags.contains(new Pair(name, value));
	}

	private void removeSelections(String name, String value) {
		((Policy)domainObject).removeTag(name, value);
	}
	  
	public Composite initializeSectionHeading(Composite parent, String title) {
		Composite labelComposite = new Composite(parent, SWT.NONE);
		labelComposite.setBackground(getBackground());
		FormLayout layout = new FormLayout();
		labelComposite.setLayout(layout);

		Label sectionLabel = new Label(labelComposite, SWT.NONE);
		sectionLabel.setText(title);
		sectionLabel.setBackground(getBackground());
		sectionLabel.setForeground(ColorBundle.CE_MED_BLUE);
		FormData sectionLabelData = new FormData();
		sectionLabelData.left = new FormAttachment(0, 100, SPACING);
		sectionLabelData.top = new FormAttachment(0, 100, SPACING);
		sectionLabel.setLayoutData(sectionLabelData);

		Label separator = new Label(labelComposite, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		separator.setBackground(getBackground());
		FormData separatorData = new FormData();
		separatorData.top = new FormAttachment(sectionLabel, 2);
		separatorData.left = new FormAttachment(0, 100, SPACING);
		separatorData.right = new FormAttachment(100);
		separator.setLayoutData(separatorData);

		return labelComposite;
	}

	public abstract void initializeContents();

	/**
     * 
     */
	private void createDescriptionText() {
		int style = (isEditable()) ? SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
				| SWT.WRAP : SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP
				| SWT.READ_ONLY;
		Text text = new Text(footerComposite, style);
		text.setTextLimit(2048);
		text.setEditable(isEditable());
		descriptionText = text;
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				Text t = (Text) e.getSource();
				String newDescription = t.getText();
				if (newDescription == null) {
					newDescription = "";
				}
				String oldDescription = getDescription();
				if (oldDescription == null) {
					oldDescription = "";
				}
				if (!newDescription.equals(oldDescription)) {
					PropertyUndoElement undoElement = new PropertyUndoElement();
					undoElement.setProp(ObjectProperty.DESCRIPTION);
					undoElement.setOldValue(oldDescription);
					undoElement.setNewValue(newDescription);
					GlobalState.getInstance().addUndoElement(undoElement);
					setDescription(newDescription);
				}
			}
		});

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == '\t') {
					if ((e.stateMask & (SWT.CONTROL | SWT.ALT | SWT.SHIFT)) == 0) {
						e.doit = false;
						descriptionText.traverse(SWT.TRAVERSE_TAB_NEXT);
					}
				}
			}
		});
		String description = getDescription();
		if (description == null) {
			description = "";
		}
		text.setText(description);
	}

	// --Relayout Behavior----

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bluejungle.destiny.policymanager.editor.IEditorPanel#relayout()
	 */
	public void relayout() {
		if (!isInitialized || ignoreRelayout) {
			return;
		}

		relayoutHeading();
		relayoutContents();
		scrolledComposite.setMinSize(leftEditorComposite.computeSize(SWT.DEFAULT,SWT.DEFAULT));

		leftEditorComposite.layout();
		// layout may not redraw everything that needs it:
		leftEditorComposite.redraw();
	}

	protected void relayoutHeading() {
	}

	protected abstract void relayoutContents();

	/**
     * 
     */
	protected void updateProperties() {
		// nameLabel.setText(getObjectName());
		String oldDescription = descriptionText.getText();
		String newDescription = getDescription();
		if (newDescription == null) {
			newDescription = "";
		}
		if (oldDescription == null) {
			oldDescription = "";
		}
		if (!newDescription.equals(oldDescription)) {
			descriptionText.setText(newDescription);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bluejungle.destiny.policymanager.editor.IEditorPanel#
	 * getControlDomainObject(int,
	 * com.bluejungle.pf.domain.destiny.common.IDSpec)
	 */
	public abstract CompositePredicate getControlDomainObject(int controlId,
			IHasId domainObject);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bluejungle.destiny.policymanager.editor.IEditorPanel#saveContents()
	 */
	public void saveContents(SaveCause cause) {
		DevelopmentStatus status = DomainObjectHelper
				.getStatus(getDomainObject());
		// Scott's fix for locking - start
		// if (status == DevelopmentStatus.NEW || status ==
		// DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT) {
		if ((isEditable())
				&& ((status == DevelopmentStatus.NEW
						|| status == DevelopmentStatus.EMPTY || status == DevelopmentStatus.DRAFT))) {
			// Scott's fix for locking - end
			PolicyServerProxy.saveEntity(getDomainObject());
		}
	}

	protected abstract EntityType getEntityType();

	/**
	 * @return Returns the editable.
	 */
	public boolean isEditable() {
		return editable;
	}

	abstract public boolean hasCustomProperties();

	/**
	 * @param editable
	 *            The editable to set.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract String getObjectName();

	public abstract String getObjectTypeLabelText();

	protected String getDisplayName() {
		return displayName;
	}

	public IHasId getDomainObject() {
		return domainObject;
	}

	public boolean canRelayout() {
		return isInitialized && !ignoreRelayout;
	}

	abstract protected Class<?> getPreviewClass();

	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
