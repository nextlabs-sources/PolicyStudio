/*
 * Created on Jun 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.axis.AxisFault;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.ConfigurableMessages;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;

/**
 * @author bmeng
 * @version $Id:
 */
public class LoginDialog extends TitleAreaDialog {

    private Text textUserName;
    private Text textPassword;
    private Combo comboServer;

    private String[] knownServers;

    private boolean loginSuccessful = false;
    private String username, password, policyServer;

    /**
     * @param parent
     */
    public LoginDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        String result = "";
        VersionFactory versionFactory = new VersionFactory();
        try {
            IVersion version = versionFactory.getVersion();
            StringBuffer versionString = new StringBuffer();
            versionString.append(version.getMajor());
            versionString.append(".");
            versionString.append(version.getMinor());
            versionString.append(".");
            versionString.append(version.getMaintenance());
            versionString.append(" (");
            versionString.append(version.getBuild());
            versionString.append(")");
            result = versionString.toString();
        } catch (InvalidVersionException exception) {
            // Shouldn't happen
            LoggingUtil.logWarning(Activator.ID,
                    "Failed to find Policy Author version.", exception);
        } catch (IOException exception) {
            LoggingUtil.logWarning(Activator.ID,
                    "Failed to find Policy Author version.", exception);
        }

        newShell.setText(ConfigurableMessages.LOGINDIALOG_WINDOW_TITLE + " "
                + result);
        newShell.setImage(ImageBundle.POLICYSTUDIO_IMG);
        
    }

    @Override
    public void create() {
        super.create();
        setTitle(DialogMessages.LOGINDIALOG_TITLE);
        setTitleImage(ImageBundle.TITLE_IMAGE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);

        final Composite root = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        root.setLayoutData(data);

        // We use a grid layout and set the size of the margins
        final GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 10;
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 10;
        root.setLayout(gridLayout);

        Label labelUserName = new Label(root, SWT.NONE);
        labelUserName.setText(DialogMessages.LOGINDIALOG_USER_NAME);

        textUserName = new Text(root, SWT.BORDER);
        textUserName.setToolTipText(DialogMessages.LOGINDIALOG_ERROR_USER_NAME);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textUserName.setLayoutData(data);

        Label labelPassword = new Label(root, SWT.NONE);
        labelPassword.setText(DialogMessages.LOGINDIALOG_PASSWORD);

        textPassword = new Text(root, SWT.PASSWORD | SWT.BORDER);
        textPassword.setToolTipText(DialogMessages.LOGINDIALOG_ERROR_PASSWORD);
        data = new GridData(GridData.FILL_HORIZONTAL);
        textPassword.setLayoutData(data);

        Label labelServer = new Label(root, SWT.RIGHT);
        labelServer.setText(DialogMessages.LOGINDIALOG_POLICY_SERVER);

        comboServer = new Combo(root, SWT.NONE);
        comboServer.setToolTipText(DialogMessages.LOGINDIALOG_ERROR_SERVER);
        knownServers = KnownPolicyServerList.getKnowPolicyServers();
        comboServer.setItems(knownServers);
        comboServer.setText(knownServers[0]);
        data = new GridData(GridData.FILL_HORIZONTAL);
        comboServer.setLayoutData(data);
        comboServer.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                String text = comboServer.getText();
                int count = comboServer.getItemCount();
                if (count > 0) {
                    comboServer.remove(0, count - 1);
                }
                for (String server : knownServers) {
                    if (server.indexOf(text) == 0) {
                        comboServer.add(server);
                    }
                }
            }
        });

        textUserName.setFocus();
        textUserName.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setMessage("", IMessageProvider.NONE);
            }
        });

        textPassword.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setMessage("", IMessageProvider.NONE);
            }
        });

        comboServer.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                setMessage("", IMessageProvider.NONE);
            }
        });
        
        return parent;
    }

    private boolean validateInputFields() {
        setMessage("", IMessageProvider.NONE);

        username = textUserName.getText();
        password = textPassword.getText();
        policyServer = comboServer.getText();

        if ((username == null) || (username.equals(""))) {
            setMessage(DialogMessages.LOGINDIALOG_ERROR_USER_NAME, IMessageProvider.ERROR);
            return false;
        } else if ((password == null) || (password.equals(""))) {
            setMessage(DialogMessages.LOGINDIALOG_ERROR_PASSWORD, IMessageProvider.ERROR);
            return false;
        } else if ((policyServer == null) || (policyServer.equals(""))) {
            setMessage(DialogMessages.LOGINDIALOG_ERROR_SERVER, IMessageProvider.ERROR);
            return false;
        }
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button buttonOk = getButton(IDialogConstants.OK_ID);
        buttonOk.setText(DialogMessages.LABEL_LOGIN);

        getButton(IDialogConstants.CANCEL_ID).setText(DialogMessages.LABEL_QUIT);
    }

    @Override
    protected void okPressed() {
        
        if (!validateInputFields()) {
            return;
        }

        username = textUserName.getText();
        password = textPassword.getText();
        policyServer = comboServer.getText();

        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                try {
                    loginSuccessful = PolicyServerProxy.login(username, password, policyServer);

                    if (!loginSuccessful) {
                        setMessage(DialogMessages.LOGINDIALOG_ERROR_INVALID, IMessageProvider.ERROR);
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                        getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
                    } else {
                        KnownPolicyServerList.addKnownPolicyServer(policyServer);

                        setReturnCode(OK);
                        close();
                    }
                } catch (LoginException exception) {
                    String errorMessage;
                    Throwable cause = exception.getCause();
                    if(cause != null) {
                        //the order is important
                        if (cause instanceof AccessDeniedFault){
                            errorMessage = exception.getMessage();
                        } else if (cause instanceof AxisFault) {
                            errorMessage = ((AxisFault)cause).getFaultString();
                        } else {
                            errorMessage = exception.getMessage();
                        }
                    } else {
                        errorMessage = exception.getMessage();
                    }
                   
                    setMessage(DialogMessages.LOGINDIALOG_ERROR_CONNECTION + "  " + errorMessage
                            , IMessageProvider.ERROR);
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                    getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
                }
            }
        });

        setMessage(DialogMessages.LOGINDIALOG_ERROR_PROGRESS, IMessageProvider.INFORMATION);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
    }

    public String getPolicyServer() {
        return this.policyServer;
    }

    public String getUsername() {
        return this.username;
    }

    private static class KnownPolicyServerList {

        private static final String KNOWN_SERVERS_STATE_STRING_KEY = "KNOWN_POLICY_SERVERS";
        private static final String KNOWN_SERVERS_STATE_STRING_DELIMETER = ",";
        private static List<String> knownServers = new ArrayList<String>();

        public static String[] getKnowPolicyServers() {
            if (knownServers.isEmpty()) {
                loadKnownServers();
            }

            Collections.sort(knownServers, new Comparator<String>() {

                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });

            return (String[]) knownServers.toArray(new String[knownServers.size()]);
        }

        public static void addKnownPolicyServer(String knownPolicyServer) {
            if (!knownServers.contains(knownPolicyServer)) {
                knownServers.add(knownPolicyServer);

                saveUpdatedKnowServersList();
            }
        }

        /**
         * @throws StatePersistenceException
         * 
         */
        private static void saveUpdatedKnowServersList() {
            filterKnownServers();
            Preferences preferences = Activator.getDefault().getPluginPreferences();
            StringBuffer servers = new StringBuffer();
            for (String server : knownServers) {
                servers.append(server);
                servers.append(KNOWN_SERVERS_STATE_STRING_DELIMETER);
            }
            servers.deleteCharAt(servers.length() - 1);
            preferences.setValue(KNOWN_SERVERS_STATE_STRING_KEY, servers.toString());
            Activator.getDefault().savePluginPreferences();
        }

        /**
         * 
         */
        private static void loadKnownServers() {
            Preferences preferences = Activator.getDefault().getPluginPreferences();
            String knownServersAsStateString = preferences.getString(KNOWN_SERVERS_STATE_STRING_KEY);
            if (knownServersAsStateString.length() == 0) {
                knownServersAsStateString = getDefaultServerFromConfig();
                knownServers.add(knownServersAsStateString);
            } else {
                String[] servers = knownServersAsStateString.split(KNOWN_SERVERS_STATE_STRING_DELIMETER);
                for (String server : servers) {
                    knownServers.add(server);
                }
                filterKnownServers();
            }
        }

        private static void filterKnownServers() {
            if (knownServers.contains("[MACHINE_NAME]:[DPMS_PORT]") && knownServers.size() > 1) {
                knownServers.remove("[MACHINE_NAME]:[DPMS_PORT]");
            }
        }

        private static String getDefaultServerFromConfig() {
            String defaultServerAddress = PolicyServerProxy.getDefaultServerAddress();
            if (defaultServerAddress.startsWith("http://")) {
                defaultServerAddress = defaultServerAddress.substring(7);
            }
            if (defaultServerAddress.startsWith("https://")) {
                defaultServerAddress = defaultServerAddress.substring(8);
            }

            return defaultServerAddress;
        }
    }
}
