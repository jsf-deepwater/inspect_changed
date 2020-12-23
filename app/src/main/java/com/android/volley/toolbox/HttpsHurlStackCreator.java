package com.android.volley.toolbox;

import android.content.Context;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by phy on 2016/12/5.<br/>
 * add support for https.<br/>
 * <p>
 * a.采用KeyStore引导创建trustmanager的方式.<br/>
 * b.采用自定义的trustmanager.<br/>
 */

public class HttpsHurlStackCreator extends HurlStack {
    final static Object LOCK = new Object();
    static SSLSocketFactory sslSocketFactory = null;

    private HttpsHurlStackCreator() {
    }

    public static HurlStack create(Context context) {
        if (sslSocketFactory == null) {
            try {
                //忽略主机验证
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    String host1 = "";

                    @Override
                    public boolean verify(String hostname, SSLSession session) {
//                        Log.e("TAG", "hostname:" + hostname);
                        //若有需要，可在这里验证主机名
                        return true;
                    }
                });


                String trustFileName = "mmc_server.cer";

//                TrustManager[] trustManagers = createTrustManager(context, trustFileName);
//                TrustManager[] trustManagers = createMyTrustManager(context, trustFileName);
                TrustManager[] trustManagers = new TrustManager[]{trustAllManager};
                SSLContext sslContext = SSLContext.getInstance("TLS", "AndroidOpenSSL");
                sslContext.init(null, trustManagers, null);

                sslSocketFactory = sslContext.getSocketFactory();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.toString());
            }
        }
        return new HurlStack(null, sslSocketFactory);
    }


    private static TrustManager trustAllManager = new X509TrustManager() {
        X509Certificate[] acceptCertificate = new X509Certificate[0];

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return acceptCertificate;
        }
    };

    /**
     * 以下两种trustmanager都需要验证服务器证书
     *
     * @param context
     * @param trustStoreFile
     * @return
     */
    private static TrustManager[] createTrustManager(Context context, String trustStoreFile) {
        InputStream is = null;
        try {
            synchronized (LOCK) {
                is = context.getResources().getAssets().open(trustStoreFile);
            }
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Certificate cert = certFactory.generateCertificate(is);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("trust", cert);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 不通过keystore引导创建,而是使用自定义的trustmanager也可以
     */
    private static TrustManager[] createMyTrustManager(Context context, String trustStoreFileName) {
        InputStream is = null;
        try {
            synchronized (LOCK) {
                is = context.getResources().getAssets().open(trustStoreFileName);
            }
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            final Certificate cert = certFactory.generateCertificate(is);
            TrustManager myTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    /**
                     * server可能不止一个证书,这里只要验证了其中一个即可.
                     */
                    boolean verifyOK = false;
                    for (X509Certificate _cert : chain) {
                        _cert.checkValidity();
                        try {
                            _cert.verify(cert.getPublicKey());
                            verifyOK = true;
                        } catch (Exception e) {
                        }
                    }
                    if (!verifyOK) throw new CertificateException();
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            return new TrustManager[]{myTrustManager};
        } catch (Exception e) {
            return null;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }

    }

}
