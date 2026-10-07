# CodeSync Android

Opens http://172.190.1.95:8082/codesync/share/<KEY> inside a WebView.

1. Open this folder in Android Studio (Ladybug or newer) and let Gradle sync
   (Android Studio generates the Gradle wrapper automatically).
2. Run on a device/emulator that can reach 172.190.1.95:8082.
3. To change the server address, edit CODE_SYNC_URL in MainActivity.java.

Cleartext HTTP is allowed via res/xml/network_security_config.xml.
Tighten it if you move to HTTPS.
