// PA_CA.h : main header file for the PA_CA DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols
#include <msi.h>
#include <Msiquery.h.>

#define MAX_LOOKUP_RETRY 3



// CPA_CAApp
// See PA_CA.cpp for the implementation of this class
//

class CPA_CAApp : public CWinApp
{
public:
	CPA_CAApp();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};

UINT fnSetupErrorPolicyServerValidation(MSIHANDLE hInstall);
UINT fnBackupConfigXML(MSIHANDLE hInstall);
UINT fnRestoreConfigXML(MSIHANDLE hInstall);
void fnRetrieveProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchPropertyValue );
void fnStoreProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchPropertyValue );
void fnWriteToInstallerLogFile(MSIHANDLE hInstall, TCHAR * tchMessage);
UINT fnIsPolicyManagerRunning( MSIHANDLE hInstall);
bool fnIsApplicationRunning( TCHAR * tchApplication );
UINT fnBackupConfigXML(MSIHANDLE hInstall);
UINT fnRestoreConfigXML(MSIHANDLE hInstall);
bool fnGetOldInstallDir(MSIHANDLE hInstall,TCHAR * tchOldInstallDir,TCHAR * tchOldProperty);
UINT fnCreateConfigurationFile(MSIHANDLE hInstall);
void fnGetPolicyManagerInstallDir( TCHAR * tchInstallDir );
UINT fnValidatePolicyServerLocation(MSIHANDLE hInstall);
UINT fnTestHostConnection (MSIHANDLE hInstall, TCHAR * tchHost, TCHAR * tchPort);