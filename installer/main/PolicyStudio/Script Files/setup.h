#ifndef SETUP_HEADER
	export prototype string MsiGetCustomActionDataAttribute( HWND,  STRING );
    export prototype INT MsiSetCustomActionDataAttribute( HWND ); 
	export prototype WSTRING GetPolicyAuthorKeyStorePassword(HWND);
    export prototype INT UpdatePAKeyStoreCfg(HWND);
	export prototype INT ValidatePAKeyStorePassword(HWND);
    export prototype FindAndReplace(STRING, STRING, STRING);        
#endif	
#define SETUP_HEADER