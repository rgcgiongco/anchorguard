    public static String computeDeviceId(Context context) {
        String string;
        Exception e10;
        MessageDigest messageDigest = null;
        try {
            String str = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.DISPLAY.length() % 10) + (Build.HOST.length() % 10) + (Build.ID.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10) + (Build.TAGS.length() % 10) + (Build.TYPE.length() % 10) + (Build.USER.length() % 10);
            string = Settings.Secure.getString(context.getContentResolver(), "android_id");
            try {
                String macAddress = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getMacAddress();
                String address = BuildConfig.FLAVOR;
                try {
                    address = BluetoothAdapter.getDefaultAdapter().getAddress();
                } catch (Exception unused) {
                }
                String str2 = str + string + macAddress + address;
                try {
                    messageDigest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e11) {
                    e11.printStackTrace();
                }
                messageDigest.update(str2.getBytes(), 0, str2.length());
                byte[] bArrDigest = messageDigest.digest();
                String str3 = new String();
                for (byte b10 : bArrDigest) {
                    int i10 = b10 & 255;
                    if (i10 <= 15) {
                        str3 = str3 + "0";
                    }
                    str3 = str3 + Integer.toHexString(i10);
                }
                str3.toUpperCase();
                return string;
            } catch (Exception e12) {
                e10 = e12;
                Utility.j4("getDeviceId", TAG, e10);
                try {
                    return Settings.Secure.getString(context.getContentResolver(), "android_id") + ((int) (Math.random() * 9000.0d)) + PlaybackException.ERROR_CODE_UNSPECIFIED;
                } catch (Exception unused2) {
                    return string;
                }
            }
        } catch (Exception e13) {
            string = null;
            e10 = e13;
        }
    }
