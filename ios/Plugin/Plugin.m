#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(HttpPlugin, "HttpPlugin",
    CAP_PLUGIN_METHOD(request, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(setCookie, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getCookies, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(deleteCookie, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(clearCookies, CAPPluginReturnPromise);
)
