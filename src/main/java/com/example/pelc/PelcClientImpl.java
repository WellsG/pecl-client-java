package com.example.pelc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class PelcClientImpl implements PelcClient, InvocationHandler {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PelcClientImpl.class);
    
    private String serverURL;
    private HttpClient httpclient;
    private static final ThreadLocal<String> pelcToken = new ThreadLocal<String>();

    private Semaphore loginSemaphore = new Semaphore(1);

    public static PelcClient create(String url) {
        final PelcClientImpl impl =
            new PelcClientImpl(url);
        return (PelcClient) Proxy.newProxyInstance(PelcClientImpl.class
            .getClassLoader(), new Class[] { PelcClient.class }, impl);
    }

    public PelcClientImpl(String serverURL) {
        LOGGER.info("Starting PELC client for server url: {}", serverURL);
        this.serverURL = serverURL;
    }

    public synchronized HttpClient client() {
        if (httpclient == null) {
            System.setProperty("sun.security.krb5.debug", "true");
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("javax.security.auth.useSubjectCredsOnly",
                    "false");
            //Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            httpclient = wrapClient(new DefaultHttpClient());
        }
        return httpclient;
    }

    public void auth() throws Exception {
        String token = execute(PelcURIs.AUTH, null);
        if (!StringUtils.isEmpty(token)) {
           /* Map<String, Object> map = convertJsonToMap(token);
            LOGGER.info("token: {}", map.get("token"));
            pelcToken.set((String) map.get("token"));*/
            JSONObject json = new JSONObject(token);
            LOGGER.info("token: {}", json.getString("token"));
            pelcToken.set(json.getString("token"));
        }
    }
    
    public Map<String, Task> importPackage(String productRelease, String brewTag, List<String> packageNames) throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("product_release", productRelease);
        params.put("brew_tag", brewTag);
        params.put("package_name", packageNames);
        String result = executePost(PelcURIs.IMPORT_PACKAGE, params);
        LOGGER.info("Import package response: {}", result);
        Map<String, Task> response = 
                new Gson().fromJson(result, new TypeToken<Map<String, Task>>(){}.getType());
        return response;
    }

    private Map<String, Object> convertJsonToMap(String token) {
        Map<String, Object> map = null;
        try {
            map = new Gson().fromJson(token, new TypeToken<Map<String, Object>>(){}.getType());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return map;
    }

    private String execute(String url, Map<String, String> params)
            throws Exception {
        StringBuffer urls = new StringBuffer();
        urls.append(serverURL).append(url);
        if (params != null) {
            final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    qparams.add(new BasicNameValuePair(param.getKey(), param
                            .getValue()));
                }
            }
            urls.append("?").append(URLEncodedUtils.format(qparams, "utf-8"));
        }
        LOGGER.info("Excute method: {}", urls.toString());
        HttpUriRequest request = new HttpGet(urls.toString());
        request.setHeader("Content-Type", "application/json");
        if (!url.equals(PelcURIs.AUTH)) {
            request.setHeader("Authorization", "Token " + pelcToken.get());
        }
        try {
            HttpResponse response = client().execute(request);
            return parseResponse(response);
        } finally {
            request.abort();
        }
    }

    private String executePost(String url, Map<String, Object> params)
            throws Exception {
        StringBuffer urls = new StringBuffer();
        urls.append(serverURL).append(url);
        String jsonParams = new Gson().toJson(params);
        LOGGER.info("Excute method: {}", urls.toString());
        LOGGER.info("Params: {}", jsonParams);
        HttpPost request = new HttpPost(urls.toString());
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        StringEntity entity = new StringEntity(jsonParams);
        entity.setContentType("application/json");
        request.setEntity(entity);
        if (!url.equals(PelcURIs.AUTH)) {
            request.setHeader("Authorization", "Token " + pelcToken.get());
        }
        try {
            HttpResponse response = client().execute(request);
            return parseResponse(response);
        } finally {
            request.abort();
        }
    }

    private String parseResponse(HttpResponse response) throws Exception {
        StringBuffer sb = new StringBuffer();
        if (response.getStatusLine().getStatusCode() >= 200
                && response.getStatusLine().getStatusCode() <= 300) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } else {
            throw new Exception("http response code error: "
                    + response.getStatusLine().getStatusCode() + "  "
                    + response.getStatusLine().getReasonPhrase());
        }
        return sb.toString();
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        // This function implements login on demand for PDC;
        // The given method is invoked. If a UNAUTHORIZED error is raised,
        // then log in and try again.

        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (!(cause.getMessage().contains("UNAUTHORIZED"))) {
                throw cause;
            }

            // Only one thread needs to auth
            if (loginSemaphore.tryAcquire()) {
                try {
                    LOGGER.info("Obtain the token for call to {}", method.getName());
                    auth();
                } finally {
                    loginSemaphore.release();
                }
            } else {
                // If we get here, some other thread is attempting to log in.
                LOGGER
                    .info("Waiting for other thread to log in for call to {}",
                          method.getName());
                loginSemaphore.acquireUninterruptibly();
                loginSemaphore.release();
            }

            try {
                return method.invoke(this, args);
            } catch (InvocationTargetException e2) {
                throw e2.getCause();
            }
        }
    }

    public static org.apache.http.client.HttpClient wrapClient(
            org.apache.http.client.HttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] chain,
                        String authType)
                        throws java.security.cert.CertificateException {
                }
            };
            X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, hostnameVerifier);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("https", 443, ssf));
            registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            ClientConnectionManager mgr = new PoolingClientConnectionManager(
                    registry);

            SPNegoSchemeFactory nsf = new SPNegoSchemeFactory();
            DefaultHttpClient httpclient = new DefaultHttpClient(mgr,
                    base.getParams());
            httpclient.getAuthSchemes().register(AuthPolicy.SPNEGO, nsf);
            Credentials credential = new Credentials() {
                public String getPassword() {
                    return null;
                }

                public Principal getUserPrincipal() {
                    return null;
                }
            };
            httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credential);
            return httpclient;
        } catch (Exception ex) {
            LOGGER.error("Wrap httpclient error.", ex);
            return null;
        }
    }

}
