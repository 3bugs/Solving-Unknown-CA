package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String TEST_URL = "https://www.dplusbook.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        request1();
        request2();
    }

    /**
     * ทดสอบการเรียกไปยังเว็บ dplusbook.com ซึ่งจะสำเร็จใน Android 5.1 ขึ้นไป
     * แต่จะ error ใน Android 5.0 หรือต่ำกว่า (java.security.cert.CertPathValidatorException:
     * Trust anchor for certification path not found.)
     */
    private void request1() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(TEST_URL);
                    URLConnection urlConnection = url.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    return convertStreamToString(is);
                } catch (javax.net.ssl.SSLHandshakeException e) {
                    return "Error: SSLHandshakeException";
                } catch (IOException e) {
                    return "Error: IOException";
                } catch (Exception e) {
                    return "Error: Exception";
                }
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
                ((TextView) findViewById(R.id.textView1)).setText(msg);
            }
        }.execute();
    }

    /**
     * ทดสอบการเรียกไปยังเว็บ dplusbook.com โดยระบุ cert ของ dplusbook.com ลงในโค้ด
     * ซึ่งจะทำให้เรียก Web Service ได้สำเร็จใน Android ทุกเวอร์ชั่น
     */
    private void request2() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                // อ่าน cert จากไฟล์รีซอร์ส (res/raw/dplusbook_com.crt)
                CertificateFactory cf = null;
                Certificate cert = null;
                InputStream caInput = null;
                try {
                    cf = CertificateFactory.getInstance("X.509");
                    caInput = new BufferedInputStream(getResources().openRawResource(R.raw.dplusbook_com));
                    cert = cf.generateCertificate(caInput);

                    Log.d(TAG, "cert=" + ((X509Certificate) cert).getSubjectDN());
                } catch (CertificateException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        caInput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // สร้าง KeyStore ที่บรรจุ cert นั้น
                KeyStore keyStore = null;
                try {
                    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(null, null);
                    keyStore.setCertificateEntry("cert", cert);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }

                // สร้าง TrustManager ที่ trust cert ใน KeyStore
                TrustManagerFactory tmf = null;
                try {
                    tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keyStore);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }

                // สร้าง SSLContext ที่ใช้งาน TrustManager
                SSLContext context = null;
                try {
                    context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }

                // กำหนดให้ URLConnection ใช้ SocketFactory จาก SSLContext ของเรา
                try {
                    URL url = new URL(TEST_URL);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setSSLSocketFactory(context.getSocketFactory());
                    InputStream is = urlConnection.getInputStream();
                    return convertStreamToString(is);
                } catch (javax.net.ssl.SSLHandshakeException e) {
                    return "Error: SSLHandshakeException";
                } catch (IOException e) {
                    return "Error: IOException";
                } catch (Exception e) {
                    return "Error: Exception";
                }
            }

            @Override
            protected void onPostExecute(String msg) {
                super.onPostExecute(msg);
                ((TextView) findViewById(R.id.textView2)).setText(msg);
            }
        }.execute();
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
