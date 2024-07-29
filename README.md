# Android wear permissions bug

This bug was initially thought to lead to a vulnerability. In a nutshell, the bug leads applications targeting Android Wear to grant some permissions without them being explicitly defined in the manifest file. A responsible disclosure process was thus initiated with Google to fix the issue (the 13/08/2015, security security bug report #182733. After further investigations, the problem turned out to be a bug in the Android SDK with no serious security implications towards users:

"Android Studio appears to be implicitly adding the permissions of all linked libraries into the generated apk for an app. Specifically, in this case it's pulling in permissions that Google-Play services requires. [...] What this means is that any app created in this manner that comes in through the Play store (or any store that displays app permissions) will correctly list all the permissions being used by the app. Consequently, there isn't any risk to users because no hidden permissions are being granted to these apps (from a platform point of view. I definitely agree that it's hidden from the manifest source in Android Studio)."

I think it is worth mentioning that if the Google Play store was not displaying the list of permissions to the user at download time, the bug would have to be considered a vulnerability. The bug and the discovery process are described below.

##Bug

The discovered bug allows the following permissions to be granted automatically to applications without the user granting them and without being explicitly defined in the manifest file:

Protection level: normal
- android.permission.ACCESS_NETWORK_STATE
- android.permission.GET_ACCOUNTS
- android.permission.READ_EXTERNAL_STORAGE

Protection level: dangerous
- android.permission.ACCESS_COARSE_LOCATION
- android.permission.INTERNET
- android.permission.USE_CREDENTIALS
- android.permission.WRITE_EXTERNAL_STORAGE

This only happens with a Phone/Wear project. With a regular Phone application, none of the permissions are granted (as expected, the permissions need to be defined in the manifest file) and the socket communication example detailed below returns an exception *SocketException: socket failed: EACCES (Permission denied)*.

The funny thing is that it is not needed to publish both the "mobile" and the "wear" modules for the bug to happen, it only require to build the "mobile" module, install the apk on a smartphone running Android 5.1 and the permissions are granted by default.

##Development environment

* OS X 10.10.4
* Android Studio 1.2.2
* Android phone/tablet: API 22: Android 5.1 (Lollipop) (Tested on Nexus 4, 6, and 9)
* Android wear: API 21: Android 5.0 (Lollipop)

##Example: Exploit android.permission.INTERNET

###Vulnerability

Sending and receiving data with socket is possible without internet permission in manifest file.

###Problem

Without the explicit permission "android.permission.INTERNET" defined in the "AndroidManifest.xml" file, socket connections to a remote server are still possible. This issue allows any program to send and receive data over the internet without the user explicitly granting this permission to the application. Both user interaction or program thread can trigger incoming/outgoing communication.

```java
String permission = "android.permission.INTERNET";
int result = getApplicationContext().checkCallingOrSelfPermission(permission);
// Print "true" even though the internet permission is not defined in the manifest file
Log.d(this.getClass().getName(), "Internet permission granted: " + (result == PackageManager.PERMISSION_GRANTED));
```

According to Google's guidelines, any dangerous permissions requested by an application may be displayed to the user and require confirmation before proceeding. The permission allowing applications to open network sockets is categorized as "dangerous".

```xml
    <permission android:name="android.permission.INTERNET"
        android:permissionGroup="android.permission-group.NETWORK"
        android:protectionLevel="dangerous" />
```

If Google Play store was hiding the permissions that are bypassing the manifest file, this vulnerability would allow any attacker to ship a legit application on Google Play store with a backdoor allowing communication with its server without the user being aware of it. The number of possible attacks leveraged by such a vulnerability could lead to serious threats (privacy leak, user tracking, remote code execution, DDoS, and many more...).

###Steps to reproduce

1. Create project Phone/Wear project in Android Studio:
    1. File > New Project
    2. Select: Phone And Tablet with Minimum SDK "API 22: Android 5.1 (Lollipop)"
    3. Select: Wear with Minimum SDK "API 21: Android 5.0 (Lollipop)"
    4. Select Blank Activity for both Phone and Wear

2. In the module "mobile":
    1. Replace MainActivity.java and activity_main.xml with the provided code
    2. Get the provided files PermissionScanner.java and permissions.xml
    3. Change server address and port constants according to your specific configuration

3. To prove that a communication channel is successfully established, get your server up and running.

4. Compile the module "mobile" (from Android Studio or Gradle), install, and run the application on a compatible device.

5. Here you go, your application is now able to send/receive messages to/from your remote server while no permission was defined in the manifest file to allow this.

##Note

The example above illustrates a simple case on how the "android.permission.INTERNET" permission can be exploited. One can easily imagine the exploit possibilities with the other granted dangerous permissions:

* android.permission.ACCESS_COARSE_LOCATION: Allows an app to access approximate location derived from network location sources such as cell towers and Wi-Fi. This would allow an attacker to silently track a user's location.
* android.permission.USE_CREDENTIALS: Allows an application to request authtokens from the AccountManager. This would allow an attacker to log into accounts (for example Google, Facebook, and Microsoft Exchange) on behalf of the user.
* android.permission.WRITE_EXTERNAL_STORAGE: Allows an application to write to external storage. An attacker could potentially store malware or other malicious data on the device external storage.
