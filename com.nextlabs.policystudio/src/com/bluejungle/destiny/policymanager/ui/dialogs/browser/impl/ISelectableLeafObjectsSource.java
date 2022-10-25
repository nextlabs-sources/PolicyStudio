package com.bluejungle.destiny.policymanager.ui.dialogs.browser.impl;

import java.util.List;

import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;

public interface ISelectableLeafObjectsSource {

	public List<LeafObject> getSelectableLeafObjects(
			LeafObjectType leafObjectType, int maxElements)
			throws SelectableLeafObjectSourceException;

	public void addListModificationListener(
			ISelectableLeafObjectListModificationListener listener);

}
