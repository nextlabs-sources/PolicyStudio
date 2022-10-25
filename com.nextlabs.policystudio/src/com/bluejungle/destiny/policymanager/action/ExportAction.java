package com.bluejungle.destiny.policymanager.action;

import java.io.File;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

import com.nextlabs.pf.destiny.importexport.IExporter;
import com.nextlabs.pf.destiny.importexport.impl.UIExporter;

@SuppressWarnings("restriction")
public class ExportAction extends AbstractExportAction {
    public ExportAction() {
        super();
        setEnabled(false);
    }
    
    @Override
    public String getText() {
        return ActionMessages.ACTION_EXPORT;
    }

    protected IExporter getExporter() {
        return new UIExporter();
    }

    public String getDefaultExtension() {
        return ".xml";
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ);
    }
}
