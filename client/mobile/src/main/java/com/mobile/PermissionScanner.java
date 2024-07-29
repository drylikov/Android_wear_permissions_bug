package com.tonybeltramelli.mobile;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by Tony Beltramelli www.tonybeltramelli.com on 17/08/15.
 */
public class PermissionScanner
{
    private Context _context;

    public PermissionScanner(Context context)
    {
        _context = context;
    }

    public String scan()
    {
        String result = "";

        try
        {
            XmlResourceParser xml = _context.getResources().getXml(R.xml.permissions);

            int eventType = xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (xml.getName().equals("permission"))
                    {
                        String name = xml.getAttributeValue(0);
                        result += "- " + name + " => permissionLevel: " + _getPermissionLevel(name) + ", isGranted: " + _isPermissionGranted(name) + "\n";
                    }
                }
                eventType = xml.next();
            }
        } catch (Exception e)
        {
            Log.e(this.getClass().getName(), e.getMessage());
        } finally
        {
            return result;
        }
    }

    private boolean _isPermissionGranted(String permissionName)
    {
        int result = _context.checkCallingOrSelfPermission(permissionName);
        return (result == PackageManager.PERMISSION_GRANTED);
    }

    private String _getPermissionLevel(String permissionName)
    {
        String protectionLevel;

        try
        {
            PermissionInfo info = _context.getPackageManager().getPermissionInfo(permissionName, PackageManager.GET_META_DATA);

            switch (info.protectionLevel)
            {
                case PermissionInfo.PROTECTION_NORMAL:
                    protectionLevel = "normal";
                    break;
                case PermissionInfo.PROTECTION_DANGEROUS:
                    protectionLevel = "dangerous";
                    break;
                case PermissionInfo.PROTECTION_SIGNATURE:
                    protectionLevel = "signature";
                    break;
                case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                    protectionLevel = "signatureOrSystem";
                    break;
                case PermissionInfo.PROTECTION_FLAG_SYSTEM:
                    protectionLevel = "system";
                    break;
                default:
                    protectionLevel = "unknown";
                    break;
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            protectionLevel = "unknown";
        }

        return protectionLevel;
    }
}
