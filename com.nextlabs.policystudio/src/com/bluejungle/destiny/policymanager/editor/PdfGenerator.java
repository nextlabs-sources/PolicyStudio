/*
 * Created on Jun 26, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.model.IPdfGenerator;
import com.bluejungle.destiny.policymanager.model.PolicyServerHelper;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.PluginUtil;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/PolicyStudio/D_Plugins/com.nextlabs.policystudio/src/com
 *          /bluejungle/destiny/policymanager/editor/PdfGenerator.java#2 $
 */

public class PdfGenerator extends PdfPageEventHelper {

	/** The PdfTemplate that contains the total number of pages. */
	private PdfTemplate total;

	/** The font that will be used. */
	private BaseFont helv;

	private String file;
	private List<DomainObjectDescriptor> descriptors;

	public PdfGenerator(String file, List<DomainObjectDescriptor> descriptors) {
		this.file = file;
		this.descriptors = descriptors;
	}

	/**
	 * @see com.lowagie.text.pdf.PdfPageEvent#onOpenDocument(com.lowagie.text.pdf.PdfWriter,
	 *      com.lowagie.text.Document)
	 */
	@Override
	public void onOpenDocument(PdfWriter writer, Document document) {
		total = writer.getDirectContent().createTemplate(100, 100);
		total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
		try {
			helv = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
					BaseFont.NOT_EMBEDDED);
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	/**
	 * @see com.lowagie.text.pdf.PdfPageEvent#onEndPage(com.lowagie.text.pdf.PdfWriter,
	 *      com.lowagie.text.Document)
	 */
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		float textBase = document.bottom() - 20;

		String text = "Report compiled ";
		float textSize = helv.getWidthPoint(text, 12);

		Calendar today = new GregorianCalendar();
		Format formatter = new SimpleDateFormat("MM/dd/yy");
		text += formatter.format(today.getTime());

		cb.beginText();
		cb.setFontAndSize(helv, 12);
		float adjust = helv.getWidthPoint("0", 12);
		cb.setTextMatrix(document.left(), textBase);
		cb.showText(text);

		text = "Page " + writer.getPageNumber() + " of ";
		textSize = helv.getWidthPoint(text, 12);

		cb.setFontAndSize(helv, 12);
		cb.setTextMatrix(document.right() - textSize - adjust, textBase);
		cb.showText(text);
		cb.endText();

		cb.addTemplate(total, document.right() - adjust, textBase);
		cb.restoreState();
	}

	/**
	 * @see com.lowagie.text.pdf.PdfPageEvent#onCloseDocument(com.lowagie.text.pdf.PdfWriter,
	 *      com.lowagie.text.Document)
	 */
	@Override
	public void onCloseDocument(PdfWriter writer, Document document) {
		total.beginText();
		total.setFontAndSize(helv, 12);
		total.setTextMatrix(0, 0);
		total.showText(String.valueOf(writer.getPageNumber() - 1));
		total.endText();
	}

	/**
	 * Generates a file with a header and a footer.
	 * 
	 * @param args
	 *            no arguments needed here
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		// step 1: creation of a document-object
		Shell shell = Display.getCurrent().getActiveShell();
		Rectangle pageSize = new Rectangle(PageSize.LETTER);
		Document document = new Document(pageSize);
		try {
			// step 2:
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(file));
			writer.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft);
			writer.setPageEvent(this);
			document.setMargins(36, 36, 36, 54);
			// step 3:
			document.open();
			Font font = new Font(Font.TIMES_ROMAN, 14, Font.ITALIC);
			font.setColor(Color.BLUE);
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			float[] widths = { 2f, 1f };
			table.setWidths(widths);

			Phrase phrase = new Phrase("NextLabs Definition Report", font);
			Paragraph paragraph = new Paragraph();
			paragraph.add(phrase);

			PdfPCell cell = new PdfPCell(paragraph);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.addCell(cell);

			paragraph = new Paragraph();
			URL urlEntry = Activator.getDefault().getBundle().getEntry(
					"/resources/images/logo.png");
			Image logoImage = Image.getInstance(urlEntry);
			Chunk logo = new Chunk(logoImage, 0, 0);
			paragraph.add(logo);
			cell = new PdfPCell(paragraph);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);

			document.add(table);

			PdfContentByte cb = writer.getDirectContent();
			cb.setLineWidth(2f);
			cb.setColorStroke(Color.BLUE);
			float currentY = writer.getVerticalPosition(false);
			currentY -= 4;
			cb.moveTo(document.left(), currentY);
			cb.lineTo(document.right(), currentY);
			cb.stroke();

			for (int i = 0, n = descriptors.size(); i < n; i++) {
				DomainObjectDescriptor descriptor = descriptors.get(i);
				IHasId hasId = (IHasId) PolicyServerProxy
						.getEntityForDescriptor(descriptor);

				if (hasId instanceof IDPolicy) {
					IPolicy policy = (IDPolicy) hasId;
					IConfigurationElement defaultElement = PluginUtil
							.getDefaultEditorPluginForPolicy();
					IConfigurationElement editor = PluginUtil
							.getEditorPluginForDomainObject(policy);
					if (editor == null) {
						editor = defaultElement;
					}
					if (editor != null) {
						IConfigurationElement pdfGen = PluginUtil
								.getPdfPluginForContext(editor
										.getAttribute("context"));
						String contributor = pdfGen.getContributor().getName();
						Bundle bundle = Platform.getBundle(contributor);
						try {
							Class myClass = bundle.loadClass(pdfGen
									.getAttribute("class"));
							Constructor constructor[] = myClass
									.getConstructors();
							IPdfGenerator generator = (IPdfGenerator) constructor[0]
									.newInstance(new Object[] { policy,
											descriptor, document });
							generator.generate();
						} catch (IllegalArgumentException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (InstantiationException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (IllegalAccessException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (InvocationTargetException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (ClassNotFoundException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						}
					}
				} else if (hasId instanceof IDSpec) {
					IDSpec spec = (IDSpec) hasId;
					String type = PolicyServerHelper
							.getTypeFromComponentName(spec.getName());
					IConfigurationElement pdfGen = PluginUtil
							.getPdfPluginForContext(type);
					if (pdfGen != null) {
						String contributor = pdfGen.getContributor().getName();
						Bundle bundle = Platform.getBundle(contributor);
						try {
							Class myClass = bundle.loadClass(pdfGen
									.getAttribute("class"));
							Constructor constructor[] = myClass
									.getConstructors();
							IPdfGenerator generator = (IPdfGenerator) constructor[0]
									.newInstance(new Object[] { spec,
											descriptor, document });
							generator.generate();
						} catch (IllegalArgumentException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (InstantiationException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (IllegalAccessException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (InvocationTargetException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						} catch (ClassNotFoundException e) {
							LoggingUtil.logError(Activator.ID, "error", e);
						}
					}
				}

				if (i < n - 1) {
					cb = writer.getDirectContent();
					cb.setColorStroke(Color.LIGHT_GRAY);
					cb.setLineWidth(0.5f);
					currentY = writer.getVerticalPosition(false);
					currentY -= 16;
					cb.moveTo(document.left(), currentY);
					cb.lineTo(document.right(), currentY);
					cb.stroke();
				}
			}
		} catch (DocumentException de) {
			MessageDialog.openError(shell, "Error", de.getMessage());
		} catch (IOException ioe) {
			MessageDialog.openError(shell, "Error", ioe.getMessage());
		}
		document.close();
	}
}
