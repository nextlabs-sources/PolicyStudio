/*
 * Created on Mar 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.bluejungle.pf.destiny.lib.DODDigest;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates
 *          .xml#2 $:
 */

public class PolicyTransfer extends ByteArrayTransfer {

	private static final String TYPENAME = "com.bluejungle.policy";
	private static final int TYPEID = registerType(TYPENAME);
	private static PolicyTransfer _instance = new PolicyTransfer();

	/**
	 * Constructor
	 * 
	 */
	public PolicyTransfer() {
		super();
	}

	public static PolicyTransfer getInstance() {
		return _instance;
	}

	@Override
	public void javaToNative(Object object, TransferData transferData) {
		if (object == null || !(object instanceof DODDigest[]))
			return;

		if (isSupportedType(transferData)) {
			DODDigest[] descriptors = (DODDigest[]) object;
			try {
				// write data to a byte array and then ask super to convert to
				// pMedium
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream writeOut = new DataOutputStream(out);
				for (int i = 0, length = descriptors.length; i < length; i++) {
					DODDigest descriptor = descriptors[i];
					writeOut.writeUTF(descriptor.getName());
					writeOut.writeUTF(descriptor.getType());
				}
				byte[] buffer = out.toByteArray();
				writeOut.close();

				super.javaToNative(buffer, transferData);

			} catch (IOException e) {
			}
		}
	}

	@Override
	public Object nativeToJava(TransferData transferData) {
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer == null)
				return null;

			DODDigest[] data = new DODDigest[0];
			List<DODDigest> digestList = new ArrayList<DODDigest>();
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);
				while (readIn.available() > 0) {
					String name = readIn.readUTF();
					String type = readIn.readUTF();
					// EntityType entityType = EntityType.forName(type);
					DODDigest digest;
					if (type.equals("POLICY")) {
						digest = EntityInfoProvider.getPolicyDescriptor(name);
					} else if (type.equals("FOLDER")) {
						digest = EntityInfoProvider
								.getPolicyFolderDescriptor(name);
					} else {
						digest = EntityInfoProvider
								.getComponentDescriptor(name);
					}
					digestList.add(digest);
				}
				readIn.close();
				data = (DODDigest[]) digestList
						.toArray(new DODDigest[digestList.size()]);
			} catch (IOException ex) {
				return null;
			}
			return data;
		}

		return null;
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPENAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}
}
