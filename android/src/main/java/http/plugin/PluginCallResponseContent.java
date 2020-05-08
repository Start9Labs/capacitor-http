package http.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

class PluginCallResponseContent {
    private final JSObject successRes;
    private final Exception errorRes;

    private PluginCallResponseContent(JSObject successRes, Exception errorRes){
        this.successRes = successRes;
        this.errorRes = errorRes;
    }

    static PluginCallResponseContent success(JSObject successRes) {
        return new PluginCallResponseContent(successRes, null);
    }

    static PluginCallResponseContent error(Exception e) {
        return new PluginCallResponseContent(null, e);
    }

    void respondTo(PluginCall call) {
        if(errorRes != null){
            call.reject(errorRes.getMessage(), errorRes);
        } else {
            call.resolve(successRes);
        }
    }
}
