package com.bluejungle.destiny.policymanager.model;

import com.lowagie.text.DocumentException;

public interface IPdfGenerator {
	void generate() throws DocumentException;
}
