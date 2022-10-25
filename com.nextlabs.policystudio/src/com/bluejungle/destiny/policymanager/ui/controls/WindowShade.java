/*
 * Created on Feb 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ui.FontBundle;
import com.bluejungle.destiny.policymanager.util.ResourceManager;

/**
 * @author bmeng
 */

public class WindowShade extends Composite {
	private List<String> panelNames = new ArrayList<String>();
	private List<Composite> panels = new ArrayList<Composite>();
	private List<CLabel> buttons = new ArrayList<CLabel>();
	private Composite top = null;
	private Composite middle = null;
	private Composite bottom = null;
	private Composite hidden = null;
	private static final int BUTTON_HEIGHT = 18;
	private int currentPanel = 0;
	private Color[] colorGradient = null;
	private Color[] hoverColorGradient = null;
	private Color color = null;
	private Color lightColor = null;
	private Color lighterColor = null;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 */
	public WindowShade(Composite parent, int style) {
		super(parent, style);
		ResourceManager.getColor(SWT.COLOR_BLUE);
		color = ResourceManager.getColor(163, 178, 204);
		lightColor = ResourceManager.getColor(177, 190, 212);
		lighterColor = ResourceManager.getColor(225, 229, 239);
		colorGradient = new Color[] {
				ResourceManager.getColor(SWT.COLOR_DARK_GRAY), color,
				lightColor, ResourceManager.getColor(SWT.COLOR_WHITE) };
		hoverColorGradient = new Color[] { lighterColor, lightColor };
		setupLayout();
	}

	/**
	 * @param name
	 * @param panel
	 */
	public void addPanel(String name, Composite panel) {
		panelNames.add(name);
		panels.add(panel);
		addButton(name);
		panel.setParent(hidden);
	}

	/**
	 * @param name
	 */
	private void addButton(String name) {
		CLabel button = new CLabel(top, SWT.LEFT);
		button.setFont(FontBundle.DEFAULT_BOLD);
		button.setText(name);
		button.setBackground(colorGradient, new int[] { 5, 95, 100 }, true);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = BUTTON_HEIGHT;
		button.setLayoutData(data);

		buttons.add(button);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				CLabel clickedButton = (CLabel) e.getSource();
				int index = buttons.indexOf(clickedButton);
				setOpenShade(index);
			}
		});
		button.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				CLabel button = (CLabel) e.getSource();
				int index = buttons.indexOf(button);
				if (index == currentPanel)
					return;
				button.setBackground(hoverColorGradient, new int[] { 100 },
						true);
			}
			@Override
			public void mouseExit(MouseEvent e) {
				CLabel button = (CLabel) e.getSource();
				int index = buttons.indexOf(button);
				if (index == currentPanel)
					return;

				button.setBackground(colorGradient, new int[] { 5, 95, 100 },
						true);
			}
		});
		button.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				CLabel button = (CLabel) e.getSource();
				int index = buttons.indexOf(button);
				if (index == currentPanel)
					button.setCursor(ResourceManager
							.getCursor(SWT.CURSOR_ARROW));
				else
					button
							.setCursor(ResourceManager
									.getCursor(SWT.CURSOR_HAND));
			}
		});
	}

	private void setupLayout() {
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		top = new Composite(this, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL);
		top.setLayoutData(data);
		layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		top.setLayout(layout);

		middle = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		middle.setLayoutData(data);
		layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		middle.setLayout(layout);

		bottom = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		bottom.setLayoutData(data);
		layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		bottom.setLayout(layout);

		hidden = new Composite(this, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		data.heightHint = 0;
		hidden.setLayoutData(data);
		layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		hidden.setLayout(layout);
	}

	public void relayout() {
		for (int i = 0; i <= currentPanel && i < buttons.size(); i++) {
			CLabel button = (CLabel) buttons.get(i);
			button.setParent(top);
			button.setBackground(colorGradient, new int[] { 5, 95, 100 }, true);
			if (i > 0 && i < currentPanel) {
				button.moveAbove(buttons.get(i + 1));
			}
		}

		for (int i = currentPanel + 1; i < buttons.size(); i++) {
			CLabel button = buttons.get(i);
			button.setParent(bottom);
			button.setBackground(colorGradient, new int[] { 5, 95, 100 }, true);
			if (i > currentPanel) {
				button.moveBelow(buttons.get(i - 1));
			}
		}

		for (int i = 0, n = panels.size(); i < n; i++) {
			Composite panel = panels.get(i);
			if (i == currentPanel) {
				panel.setParent(middle);
			} else {
				panel.setParent(hidden);
			}
		}

		// resize the height of the bottom composite
		GridData data = (GridData) bottom.getLayoutData();
		if (currentPanel == panels.size() - 1) {
			data.heightHint = 0;
		} else {
			data.heightHint = SWT.DEFAULT;
		}
		bottom.setLayoutData(data);

		layout(true, true);
	}

	public void setOpenShade(int index) {
		if (index == currentPanel)
			return;

		currentPanel = index;
		relayout();
	}
}
