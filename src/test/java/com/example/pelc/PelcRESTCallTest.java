package com.example.pelc;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.junit.Before;
import org.junit.Test;


public class PelcRESTCallTest {
    
    private PelcClient client;

    @Before
    public void setUp(){
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("jsse.enableSNIExtension", "false");

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        client = PELCUtil.setUp();
    }

    @Test
    public void testAuth() throws Exception {
        client.auth();
    }

    @Test
    public void testImportPackage() throws Exception {
        String release = "rhel-7-0";
        String brewTag = "rhel-7.0-candidate";
        String packageName = "ipa";
        client.importPackage(release, brewTag, packageName);
    }
}
