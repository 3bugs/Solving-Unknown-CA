# Solving-Unknown-CA

A project demonstrates how to solve `SSLHandshakeException: CertPathValidatorException: Trust anchor for certification path not found` by configuring `HttpsURLConnection` to trust a specific CA.

Adapted from Android Developer Doc at http://developer.android.com/training/articles/security-ssl.html

>**Note:** The CA of dplusbook.com is unknown in Android 5.0 and below.

Android 4.4.4 | Android 5.1
------------- | -------------
The CA of `dplusbook.com` is unknown. | The CA of `dplusbook.com` is known.
![Result on Android 4.4.4](/screenshots/android-4-4-4b.png) | ![Result on Android 5.1](/screenshots/android-5-1b.png)
