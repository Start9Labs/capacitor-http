/*
Copyright (c) 2020 Drifty Co.
Modifications Copyright (c) 2020 Start9 Labs, LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package http.plugin;

import android.util.Log;
import java.util.concurrent.Executors;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static java.net.Proxy.Type.DIRECT;
import static java.net.Proxy.Type.HTTP;
import static java.net.Proxy.Type.SOCKS;

/**
 * Haptic engine plugin, also handles vibration.
 *
 * Requires the android.permission.VIBRATE permission.
 */
@NativePlugin()
public class HttpPlugin extends Plugin {
    private CookieManager cookieManager = new CookieManager();
    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void load() {
        CookieHandler.setDefault(cookieManager);
    }

    @PluginMethod()
    public void request(PluginCall call) {
        executor.execute(() -> {
            String url = call.getString("url");

            String method = call.getString("method");
            JSObject headers = call.getObject("headers");
            JSObject params = call.getObject("params");

            Log.i("HttpPlugin", "Starting http " + method + " call to " + url);
            switch (method) {
                case "DELETE":
                case "PATCH":
                case "POST":
                case "PUT":
                    Log.i("HttpPlugin", "Finishing http " + method + " call to " + url);
                    mutate(call, url, method, headers, params).respondTo(call);
                    return;
                case "GET":
                case "HEAD":
                default:
                    Log.i("HttpPlugin", "Finishing http " + method + " call to " + url);
                    get(call, url, method, headers, params).respondTo(call);
            }
        });
    }

    private PluginCallResponseContent get(PluginCall call, String urlString, String method, JSObject headers,
            JSObject params) {
        try {
            Integer connectTimeout = call.getInt("connectTimeout");
            Integer readTimeout = call.getInt("readTimeout");
            if (connectTimeout != null) {
                connectTimeout *= 1000;
            }
            if (readTimeout != null) {
                readTimeout *= 1000;
            }

            URL url = new URL(urlString);
            StringBuilder qs = new StringBuilder();
            if (url.getQuery() == null) {
                qs.append("?");
            } else {
                qs.append("&");
            }
            for (Iterator<String> paramKeys = params.keys(); paramKeys.hasNext();) {
                String paramKey = paramKeys.next();
                if (qs.length() > 1) {
                    qs.append("&");
                }
                qs.append(paramKey);
                qs.append("=");
                qs.append(params.getString(paramKey));
            }
            url = new URL(urlString + qs.toString());

            HttpURLConnection conn = makeUrlConnection(call, url, method, connectTimeout, readTimeout, headers);

            JSObject successfulRes = buildResponse(conn);
            return PluginCallResponseContent.success(successfulRes);
        } catch (Exception ex) {
            return PluginCallResponseContent.error(ex);
        }
    }

    private PluginCallResponseContent mutate(PluginCall call, String urlString, String method, JSObject headers,
            JSObject params) {
        try {
            Integer connectTimeout = call.getInt("connectTimeout");
            Integer readTimeout = call.getInt("readTimeout");
            JSObject data = call.getObject("data");
            if (connectTimeout != null) {
                connectTimeout *= 1000;
            }
            if (readTimeout != null) {
                readTimeout *= 1000;
            }

            URL url = new URL(urlString);
            StringBuilder qs = new StringBuilder();
            if (url.getQuery() == null) {
                qs.append("?");
            } else {
                qs.append("&");
            }
            for (Iterator<String> paramKeys = params.keys(); paramKeys.hasNext();) {
                String paramKey = paramKeys.next();
                if (qs.length() > 1) {
                    qs.append("&");
                }
                qs.append(paramKey);
                qs.append("=");
                qs.append(params.getString(paramKey));
            }
            url = new URL(urlString + qs.toString());

            HttpURLConnection conn = makeUrlConnection(call, url, method, connectTimeout, readTimeout, headers);

            conn.setDoOutput(true);

            setRequestBody(conn, data, headers);

            conn.connect();

            return PluginCallResponseContent.success(buildResponse(conn));
        } catch (Exception ex) {
            return PluginCallResponseContent.error(ex);
        }
    }

    private HttpURLConnection makeUrlConnection(PluginCall call, URL url, String method, Integer connectTimeout,
            Integer readTimeout, JSObject headers) throws Exception {
        HttpURLConnection conn = initUrlConnection(call, url);

        conn.setAllowUserInteraction(false);
        conn.setRequestMethod(method);

        if (connectTimeout != null) {
            conn.setConnectTimeout(connectTimeout);
        }

        if (readTimeout != null) {
            conn.setReadTimeout(readTimeout);
        }

        setRequestHeaders(conn, headers);

        return conn;
    }

    private HttpURLConnection initUrlConnection(PluginCall call, URL url) throws IOException {
        JSObject proxy = call.getObject("proxy");
        String host = proxy.getString("host");
        Integer port = proxy.getInteger("port");
        String protocol = proxy.getString("protocol");

        if (host != null && port != null) {
            Proxy p;
            switch (protocol) {
                case "SOCKS":
                    p = new Proxy(SOCKS, new InetSocketAddress(host, port));
                    break;
                case "HTTP":
                    p = new Proxy(HTTP, new InetSocketAddress(host, port));
                    break;
                default:
                    p = new Proxy(DIRECT, new InetSocketAddress(host, port));
                    break;
            }
            return (HttpURLConnection) url.openConnection(p);
        }
        return (HttpURLConnection) url.openConnection();
    }

    @SuppressWarnings("unused")
    @PluginMethod()
    public void setCookie(PluginCall call) {
        String url = call.getString("url");
        String key = call.getString("key");
        String value = call.getString("value");

        URI uri = getUri(url);
        if (uri == null) {
            call.reject("Invalid URL");
            return;
        }

        cookieManager.getCookieStore().add(uri, new HttpCookie(key, value));

        call.resolve();
    }

    @SuppressWarnings("unused")
    @PluginMethod()
    public void getCookies(PluginCall call) {
        String url = call.getString("url");

        URI uri = getUri(url);
        if (uri == null) {
            call.reject("Invalid URL");
            return;
        }

        List<HttpCookie> cookies = cookieManager.getCookieStore().get(uri);

        JSArray cookiesArray = new JSArray();

        for (HttpCookie cookie : cookies) {
            JSObject ret = new JSObject();
            ret.put("key", cookie.getName());
            ret.put("value", cookie.getValue());
            cookiesArray.put(ret);
        }

        JSObject ret = new JSObject();
        ret.put("value", cookiesArray);
        call.resolve(ret);
    }

    @SuppressWarnings("unused")
    @PluginMethod()
    public void deleteCookie(PluginCall call) {
        String url = call.getString("url");
        String key = call.getString("key");

        URI uri = getUri(url);
        if (uri == null) {
            call.reject("Invalid URL");
            return;
        }

        List<HttpCookie> cookies = cookieManager.getCookieStore().get(uri);

        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equals(key)) {
                cookieManager.getCookieStore().remove(uri, cookie);
            }
        }

        call.resolve();
    }

    @SuppressWarnings("unused")
    @PluginMethod()
    public void clearCookies(PluginCall call) {
        cookieManager.getCookieStore().removeAll();
        call.resolve();
    }

    private JSObject buildResponse(HttpURLConnection conn) throws IOException {
        try {
            int code = conn.getResponseCode();
            JSObject headers = makeResponseHeaders(conn);
            InputStream stream = conn.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
            }
            in.close();

            JSObject ret = new JSObject();
            ret.put("status", code);
            ret.put("headers", headers);
            ret.put("data", builder.toString());

            return ret;
        } catch (IOException e) {
            String eMessage = e.getMessage();
            Log.e("HttpClient", "Errored in response: " + e.getMessage(), e);
            int code = 500;
            JSObject headers = makeResponseHeaders(conn);
            InputStream stream = conn.getErrorStream();
            try {
                code = conn.getResponseCode();
            } catch (Exception _) {}

            String data = "";
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    builder.append(line);
                }
                in.close();
                data = builder.toString();
            } catch (Exception _) {}

            JSObject ret = new JSObject();
            ret.put("status", code);
            ret.put("headers", headers);
            ret.put("eMessage", eMessage);
            ret.put("data", data);

            return ret;
        }
    }

    private JSObject makeResponseHeaders(HttpURLConnection conn) {
        JSObject ret = new JSObject();

        for (Map.Entry<String, List<String>> entries : conn.getHeaderFields().entrySet()) {
            String val = String.join(", ", entries.getValue());
            ret.put(entries.getKey(), val);
        }
        return ret;
    }

    private void setRequestHeaders(HttpURLConnection conn, JSObject headers) {
        Iterator<String> keys = headers.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = headers.getString(key);
            conn.setRequestProperty(key, value);
        }
    }

    private void setRequestBody(HttpURLConnection conn, JSObject data, JSObject headers)
            throws IOException, JSONException {
        String contentType = conn.getRequestProperty("Content-Type");

        if (contentType != null) {
            if (contentType.contains("application/json")) {
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(data.toString());
                os.flush();
                os.close();
            } else if (contentType.contains("application/x-www-form-urlencoded")) {

                StringBuilder builder = new StringBuilder();

                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object d = data.get(key);
                    if (d != null) {
                        builder.append(key + "=" + URLEncoder.encode(d.toString(), "UTF-8"));
                        if (keys.hasNext()) {
                            builder.append("&");
                        }
                    }
                }

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(builder.toString());
                os.flush();
                os.close();
            } else if (contentType.contains("multipart/form-data")) {
                FormUploader uploader = new FormUploader(conn);

                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();

                    String d = data.get(key).toString();
                    uploader.addFormField(key, d);
                }
                uploader.finish();
            }
        }
    }

    private URI getUri(String url) {
        try {
            return new URI(url);
        } catch (Exception ex) {
            return null;
        }
    }
}