// PA_CA.cpp : Defines the initialization routines for the DLL.
//
//(VersionNT=500 And ServicePackLevel>3) Or (VersionNT=501 And ServicePackLevel>1) Or (VersionNT=502)
#include "stdafx.h"
#include "PA_CA.h"
#include <tlhelp32.h>
#include <Winsock2.h>
#include <Ws2tcpip.h>


#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//
//	Note!
//
//		If this DLL is dynamically linked against the MFC
//		DLLs, any functions exported from this DLL which
//		call into MFC must have the AFX_MANAGE_STATE macro
//		added at the very beginning of the function.
//
//		For example:
//
//		extern "C" BOOL PASCAL EXPORT ExportedFunction()
//		{
//			AFX_MANAGE_STATE(AfxGetStaticModuleState());
//			// normal function body here
//		}
//
//		It is very important that this macro appear in each
//		function, prior to any calls into MFC.  This means that
//		it must appear as the first statement within the 
//		function, even before any object variable declarations
//		as their constructors may generate calls into the MFC
//		DLL.
//
//		Please see MFC Technical Notes 33 and 58 for additional
//		details.
//

// CPA_CAApp

BEGIN_MESSAGE_MAP(CPA_CAApp, CWinApp)
END_MESSAGE_MAP()


// CPA_CAApp construction

CPA_CAApp::CPA_CAApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CPA_CAApp object

CPA_CAApp theApp;


// CPA_CAApp initialization

BOOL CPA_CAApp::InitInstance()
{
	CWinApp::InitInstance();

	return TRUE;
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnBackupConfigXML(MSIHANDLE hInstall)
//	Purpose: Copies the config.xml file to the temp folder for preservation
//	Author: Michael Byrns
//  History: Created 11 September 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnBackupConfigXML(MSIHANDLE hInstall)
{
	TCHAR tchOldProperty[MAX_PATH];
	TCHAR *tempvar;
	tempvar = getenv( "TEMP" );
	TCHAR tchConfigPath[MAX_PATH] = TEXT("plugins\\com.bluejungle.destiny.policymanager\\config\\config.xml");
	TCHAR tchOldInstallDir[MAX_PATH];
	TCHAR tchTempPath[MAX_PATH];

	fnRetrieveProperty(hInstall,TEXT("PropertyCode"),tchOldProperty);
	if (!fnGetOldInstallDir(hInstall,tchOldInstallDir,tchOldProperty))
	{
		fnWriteToInstallerLogFile(hInstall, TEXT("fnBackupConfigXML Old Product Path failed"));
		return ERROR_SUCCESS;
	}

	fnStoreProperty(hInstall,TEXT("InstallPath"),tchOldInstallDir);

	lstrcat(tchOldInstallDir,tchConfigPath);
	lstrcpy(tchTempPath,tempvar);
	lstrcat(tchTempPath,TEXT("\\PA_config.xml"));
	CopyFile(tchOldInstallDir,tchTempPath,FALSE);
	return ERROR_SUCCESS;
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnRestoreConfigXML(MSIHANDLE hInstall)
//	Purpose: Copies the config.xml file back to the target directory after the upgrade
//	Author: Michael Byrns
//  History: Created 11 September 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnRestoreConfigXML(MSIHANDLE hInstall)
{
	TCHAR *tempvar;
	tempvar = getenv( "TEMP" );
	TCHAR tchTempPath[MAX_PATH];
	TCHAR tchInstallPath[MAX_PATH];
	TCHAR tchConfigPath[MAX_PATH] = TEXT("plugins\\com.bluejungle.destiny.policymanager_5.0\\config\\config.xml");

	lstrcpy(tchTempPath,tempvar);
	lstrcat(tchTempPath,TEXT("\\PA_config.xml"));

	fnGetPolicyManagerInstallDir( tchInstallPath);
	
	lstrcat(tchInstallPath,tchConfigPath);
	SetFileAttributes(tchInstallPath,FILE_ATTRIBUTE_NORMAL);
	CopyFile(tchTempPath,tchInstallPath,FALSE);

	return ERROR_SUCCESS;
}


//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnRetrieveProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchPropertyValue)
//	Purpose: Retrieve a stored propery for use primarily in the upgrade
//	Author: Michael Byrns
//  History: Created 7 September 2007
//////////////////////////////////////////////////////////////////////////////////
void fnRetrieveProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchPropertyValue )
{

	HKEY hKey;
	TCHAR tchKey[MAX_PATH] = TEXT("Software\\Classes\\Installer\\Properties\\");
	DWORD dwBufferSize,dwType;
	TCHAR tchProductCode[MAX_PATH];
	UINT uResult;

	dwBufferSize = MAX_PATH;
	uResult = MsiGetProperty(hInstall,TEXT("ProductCode"),tchProductCode,&dwBufferSize);

	if (uResult != ERROR_SUCCESS )
	{
		fnWriteToInstallerLogFile(hInstall, TEXT("fnRetrieveProperty MsiGetProperty[1] failed"));
		return;
	}
	lstrcat(tchKey,tchProductCode);

	if (RegOpenKeyEx( HKEY_LOCAL_MACHINE, tchKey, 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS)
	{
		dwBufferSize = MAX_PATH;
		if ( RegQueryValueEx( hKey, tchProperyName, 0, &dwType, (LPBYTE)tchPropertyValue, &dwBufferSize) != ERROR_SUCCESS)
			fnWriteToInstallerLogFile(hInstall, TEXT("fnRetrieveProperty RegQueryValueEx failed"));
		RegCloseKey(hKey);
	}
	else
		fnWriteToInstallerLogFile(hInstall, TEXT("fnRetrieveProperty RegOpenKeyEx failed"));
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnStoreProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchProperyValue)
//	Purpose: Stores a  propery for use primarily in the upgrade
//	Author: Michael Byrns
//  History: Created 11 September 2007
//////////////////////////////////////////////////////////////////////////////////
void fnStoreProperty(MSIHANDLE hInstall,TCHAR * tchProperyName,TCHAR * tchPropertyValue )
{
	HKEY hKey;
	TCHAR tchKey[] = TEXT("Software\\Classes\\Installer\\Properties\\");
	DWORD  dwDisposition; 
	TCHAR tchProductCode[MAX_PATH];
	UINT uResult;
	DWORD dwBufferSize = MAX_PATH;

	uResult = MsiGetProperty(hInstall,TEXT("ProductCode"),tchProductCode,&dwBufferSize);

	if (uResult != ERROR_SUCCESS )
	{
		fnWriteToInstallerLogFile(hInstall, TEXT("fnStoreProperty MsiGetProperty[1] failed"));
		return;
	}
	lstrcat(tchKey,tchProductCode);


	if ( RegCreateKeyEx(HKEY_LOCAL_MACHINE,tchKey,0,NULL,REG_OPTION_NON_VOLATILE,KEY_ALL_ACCESS,NULL,&hKey,&dwDisposition) == ERROR_SUCCESS)
	{
		if ( RegSetValueEx( hKey, tchProperyName, 0, REG_SZ, (LPBYTE)tchPropertyValue, lstrlen(tchPropertyValue)) != ERROR_SUCCESS)
			fnWriteToInstallerLogFile(hInstall, TEXT("fnStoreProperty RegSetValueEx failed"));
		RegCloseKey(hKey);
	}
	else
		fnWriteToInstallerLogFile(hInstall, TEXT("fnStoreProperty RegCreateKeyEx failed"));
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: void fnWriteToInstallrLogFile(MSIHANDLE hInstall, TCHAR * tchMessage)
//	Purpose: Writes to the installer log file
//	Author: Michael Byrns
//  History: Created 1 October 2007
//////////////////////////////////////////////////////////////////////////////////
void fnWriteToInstallerLogFile(MSIHANDLE hInstall, TCHAR * tchMessage)
{
	PMSIHANDLE hRec;
	TCHAR tchBuffer[MAX_PATH];
	DWORD dwLength;
	TCHAR tchFullErrorMessage[MAX_PATH];

	lstrcpy(tchFullErrorMessage,TEXT("Custom Action Logging "));
	lstrcat(tchFullErrorMessage,tchMessage);
	dwLength = lstrlen(tchFullErrorMessage);
	if ( dwLength <= 0 )
		return;

	hRec = MsiCreateRecord(1);                  
	
	if (!hRec)
		return;

	MsiRecordSetString(hRec, 1, tchFullErrorMessage);        
	MsiRecordSetString(hRec, 0, TEXT("[1]"));       
	MsiFormatRecord(hInstall, hRec, tchBuffer, &dwLength); 
	MsiProcessMessage(hInstall,INSTALLMESSAGE_INFO, hRec);                 

}
//////////////////////////////////////////////////////////////////////////////////
//	Function Name: UINT fnIsPolicyManagerRunning(MSIHANDLE hInstall)
//	Purpose: Take a snapshot and see if the policy manager is running
//	Author: Michael Byrns
//  History: Created 11 September 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnIsPolicyManagerRunning( MSIHANDLE hInstall)
{
	if ( fnIsApplicationRunning(TEXT("policymanager.exe")))
	{
		MsiSetProperty(hInstall,TEXT("ProceedWithInstall"),TEXT("0"));
		MsiSetProperty(hInstall,TEXT("PolicyManagerRunning"),TEXT("1"));
		fnWriteToInstallerLogFile(hInstall, TEXT("fnIsPolicyManagerRunning running"));
	}
	else
	{
		MsiSetProperty(hInstall,TEXT("ProceedWithInstall"),TEXT("1"));
		MsiSetProperty(hInstall,TEXT("PolicyManagerRunning"),TEXT("0"));
		fnWriteToInstallerLogFile(hInstall, TEXT("fnIsPolicyManagerRunning not running"));
	}
	return ERROR_SUCCESS;

}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: bool fnIsApplicationRunning(TCHAR * tchApplication )
//	Purpose: Take a snapshot and see if the named exe is currently running
//	Author: Michael Byrns
//  History: Created 12 September 2007
//////////////////////////////////////////////////////////////////////////////////
bool fnIsApplicationRunning( TCHAR * tchApplication )
{
	bool bAppIsRunning = false;
	HANDLE         hProcessSnap = NULL; 
    BOOL           bRet      = FALSE; 
    PROCESSENTRY32 pe32      = {0}; 
 
    //  Take a snapshot of all processes in the system. 

    hProcessSnap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0); 

    if (hProcessSnap == INVALID_HANDLE_VALUE) 
        return bAppIsRunning; 
 

    pe32.dwSize = sizeof(PROCESSENTRY32); 
	if (!Process32First(hProcessSnap, &pe32)) 
		return bAppIsRunning; 


	do
	{
		if ( lstrcmpi(pe32.szExeFile,tchApplication) == 0 )
		{
			bAppIsRunning = true;
			break;
		}

	} while( Process32Next( hProcessSnap, &pe32 ) );


    CloseHandle (hProcessSnap); 

	return bAppIsRunning;
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: bool fnGetOldInstallDir(MSIHANDLE hInstall,TCHAR * tchOldInstallDir,TCHAR * tchOldProperty)
//	Purpose: Try to retrieve the old install path based on product code
//	Author: Michael Byrns
//  History: Created 11 September 2007
//////////////////////////////////////////////////////////////////////////////////
bool fnGetOldInstallDir(MSIHANDLE hInstall,TCHAR * tchOldInstallDir,TCHAR * tchOldProperty)
{
	TCHAR tchKey[MAX_PATH] = TEXT("Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\");
	HKEY hKey;
	DWORD dwBufferSize,dwType;

	lstrcat(tchKey,tchOldProperty);


	if (RegOpenKeyEx( HKEY_LOCAL_MACHINE, tchKey, 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS)
	{
		dwBufferSize = MAX_PATH;
		if ( RegQueryValueEx( hKey, TEXT("InstallLocation"), 0, &dwType, (LPBYTE)tchOldInstallDir, &dwBufferSize) != ERROR_SUCCESS)
		{
			fnWriteToInstallerLogFile(hInstall, TEXT("fnGetOldInstallDir RegQueryValueEx failed"));
			RegCloseKey(hKey);
			return false;
		}
		RegCloseKey(hKey);
	}
	else
	{
		fnWriteToInstallerLogFile(hInstall, TEXT("fnGetOldInstallDir RegOpenKeyEx failed"));
		return false;
	}

	return true;
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnCreateConfigurationFile(MSIHANDLE hInstall)
//	Purpose: Replaces place holders in config.xml with machine name and port
//	Author: Michael Byrns
//  History: Created 7 September 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnCreateConfigurationFile(MSIHANDLE hInstall)
{
	TCHAR tchInstallDir[MAX_PATH];
	TCHAR tchConfigXML[MAX_PATH];
	DWORD dwBufferSize = MAX_PATH;
	WIN32_FIND_DATA FindFileData;
	HANDLE hFind;
	CFile fConfigFile;
	CFileException exception;
	CString strBuffer = TEXT("DPSLocation=https://");
	TCHAR tchBuffer[MAX_PATH];
	dwBufferSize = MAX_PATH;

	fnGetPolicyManagerInstallDir(tchInstallDir);

	lstrcpy(tchConfigXML,tchInstallDir);
	lstrcat(tchConfigXML,TEXT("plugins\\com.nextlabs.policystudio_5.0\\config\\config.xml"));

	hFind = FindFirstFile(tchConfigXML, &FindFileData);
	if ( hFind )
	{
		SetFileAttributes(tchConfigXML,FILE_ATTRIBUTE_NORMAL);
		FindClose(hFind);
		if( fConfigFile.Open( tchConfigXML, CFile::modeCreate | CFile::modeWrite , &exception ) )
		{
			fnRetrieveProperty(hInstall,TEXT("PolicyServer"),tchBuffer);
			strBuffer = TEXT("DPSLocation=https://");
			strBuffer += tchBuffer;
			fConfigFile.Write(strBuffer,strBuffer.GetLength());
			fConfigFile.Write("\r\n",2);
			fConfigFile.Close();
		}
	}

	return ERROR_SUCCESS;
}
//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnGetPolicyManagerInstallDir(TCHAR * tchInstallDir )
//	Purpose: Get the install directory for Policy Manager
//	Author: Michael Byrns
//  History: Created 7 September 2007
//////////////////////////////////////////////////////////////////////////////////
void fnGetPolicyManagerInstallDir( TCHAR * tchInstallDir )
{
	HKEY hKey;
	TCHAR tchKey[] = TEXT("Software\\NextLabs,Inc.\\PolicyManager");
	DWORD dwBufferSize,dwType;

	if (RegOpenKeyEx( HKEY_LOCAL_MACHINE, tchKey, 0, KEY_QUERY_VALUE, &hKey) == ERROR_SUCCESS)
	{
		dwBufferSize = MAX_PATH;
		RegQueryValueEx( hKey, TEXT("INSTALLDIR"), 0, &dwType, (LPBYTE)tchInstallDir, &dwBufferSize);
		RegCloseKey(hKey);
	}
}
//////////////////////////////////////////////////////////////////////////////////
//	Function Name: fnValidatePolicyServerLocation(MSIHANDLE hInstall)
//	Purpose: Validates the specified Policy Server location
//	Author: Michael Byrns
//  History: Created 7 September 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnValidatePolicyServerLocation(MSIHANDLE hInstall)
{
	UINT uResult;
	DWORD dwBufferSize = MAX_PATH;
	TCHAR tchPolicyServer[MAX_PATH];
	TCHAR tchPort[MAX_PATH];
	TCHAR tchHost[MAX_PATH];
	TCHAR * tchToken;
	TCHAR tchSeps[] = TEXT(":");


	MsiSetProperty(hInstall,TEXT("CanGoToNextDialog"),TEXT("0"));
	MsiGetProperty(hInstall,TEXT("POLICY_SERVER_LOCATION"),tchPolicyServer,&dwBufferSize);

	// Establish string and get the first token: 
    tchToken =  strtok( tchPolicyServer, tchSeps);
	if ( tchToken == NULL )
		return ERROR_SUCCESS;
	lstrcpy(tchHost,tchToken);

	tchToken = strtok( NULL, tchSeps ); 
	if ( tchToken == NULL )
		return ERROR_SUCCESS;
	lstrcpy(tchPort,tchToken);

	if ( fnTestHostConnection (hInstall, tchHost, tchPort) == ERROR_SUCCESS )
		MsiSetProperty(hInstall,TEXT("CanGoToNextDialog"),TEXT("1"));

	
	return ERROR_SUCCESS;
}
//////////////////////////////////////////////////////////////////////////////////
//	Function Name: UINT fnTestHostConnection (MSIHANDLE hInstall, TCHAR * tchHost, TCHAR * tchPort)
//  Purpose: Attempts to validate location specified
//	Author: Michael Byrns / part borrowed from installercommon
//  History: Created 6 October 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnTestHostConnection (MSIHANDLE hInstall, TCHAR * tchHost, TCHAR * tchPort)
{
    SOCKET ConnSocket;
    ADDRINFO* AI = NULL;
	WSADATA wsaData;
	int iResult = 0;
    int iHostLength = lstrlen(tchHost);
	bool bRetryLookup = true;


	int wsaStartResult = WSAStartup(MAKEWORD(2,2), &wsaData);
	if (wsaStartResult != NO_ERROR)
	{
		fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAStartup failed"));
		return ERROR_INSTALL_FAILURE;
	}


	int iRetryLookupCount = 0;
    while (bRetryLookup) 
	{
		int addrInfoResult = getaddrinfo(tchHost, tchPort, NULL, &AI);
		bRetryLookup = false;
		if (addrInfoResult != 0) 
		{
			iResult = 1;
			switch (addrInfoResult)
			{
			case WSANO_DATA:
				printf ("No data for host name");
				fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSANO_DATA"));

				break;
			case WSAHOST_NOT_FOUND: //Most typical of invalid host name
				fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAHOST_NOT_FOUND"));
				break;
			case WSATRY_AGAIN:
				fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSATRY_AGAIN"));
				iRetryLookupCount++;
				if (iRetryLookupCount < MAX_LOOKUP_RETRY)
				{
					iResult = 0;
					bRetryLookup = true;
				}
				break;
			default:
				fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSA default"));
			}
		}
	}

	if (iResult == 0)
	{
		//Host is valid, attempt connection
		ConnSocket = socket(AI->ai_family, SOCK_STREAM, 0);
		if (ConnSocket == INVALID_SOCKET) {
			return INVALID_SOCKET;
		}

		int connectResult = connect(ConnSocket, AI->ai_addr, (int) AI->ai_addrlen);
		if (connectResult != 0) 
		{
			iResult = 2;
			connectResult = WSAGetLastError();
			closesocket(ConnSocket);
			switch (connectResult)
			{
				case WSAEADDRNOTAVAIL:
					fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAEADDRNOTAVAIL"));
					break;
				case WSAECONNREFUSED:
					fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAECONNREFUSED"));
					break;
				case WSAENETUNREACH:
					fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAENETUNREACH"));
					iResult = 3;
					break;
				case WSAETIMEDOUT:
					fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSAETIMEDOUT"));
					break;
				default:
					fnWriteToInstallerLogFile(hInstall, TEXT("fnTestHostConnection WSA Connection failure"));
			}
		}
	}
	WSACleanup();

	if ( iResult == 0 )
		return ERROR_SUCCESS;
	else
		return ERROR_INSTALL_FAILURE;
}

//////////////////////////////////////////////////////////////////////////////////
//	Function Name: UINT fnSetupErrorPolicyServerValidation(MSIHANDLE hInstall, TCHAR * tchHost, TCHAR * tchPort)
//  Purpose: Attempts to validate location specified
//	Author: Michael Byrns / part borrowed from installercommon
//  History: Created 6 October 2007
//////////////////////////////////////////////////////////////////////////////////
UINT fnSetupErrorPolicyServerValidation (MSIHANDLE hInstall)
{   int msgboxID = MessageBox(NULL,"The Policy Management Server that you specified does not exist or does not seem to be running right now. Do you want to keep this value? To confirm your input, please click Yes, otherwise click No.","Policy Studio Installer Information", MB_ICONWARNING | MB_YESNO | MB_TOPMOST |MB_SETFOREGROUND);
	fnWriteToInstallerLogFile(hInstall, TEXT("fnSetupErrorPolicyServerValidation ENTERED"));
	switch(msgboxID){
		case IDYES:
			fnWriteToInstallerLogFile(hInstall, TEXT("fnSetupErrorPolicyServerValidation NO"));
			MsiSetProperty(hInstall,TEXT("CanGoToNextDialog"),TEXT("1"));
			break;
		case IDNO:
			fnWriteToInstallerLogFile(hInstall, TEXT("fnSetupErrorPolicyServerValidation NO"));
			MsiSetProperty(hInstall,TEXT("CanGoToNextDialog"),TEXT("0"));
			break;;
	}
	fnWriteToInstallerLogFile(hInstall, TEXT("fnSetupErrorPolicyServerValidation ENDED"));
	return ERROR_SUCCESS;
}
