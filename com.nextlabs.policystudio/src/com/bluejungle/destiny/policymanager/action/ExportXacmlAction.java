package com.bluejungle.destiny.policymanager.action;

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;

import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.nextlabs.pf.destiny.importexport.IExporter;
import com.nextlabs.pf.destiny.importexport.impl.UIXacmlExporter;

public class ExportXacmlAction extends AbstractExportAction {
    public ExportXacmlAction() {
        super();
        setEnabled(false);
    }

    @Override
    public String getText() {
        return "Export as XACML...";
    }

    protected IExporter getExporter() {
        return new UIXacmlExporter();
    }

    public String getDefaultExtension() {
        return ".xacml";
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.createFromImage(ImageBundle.XACML_EXPORT);
    }
}
