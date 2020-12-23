package x.datautil;

import android.text.TextUtils;

/**
 * Created by phy on 2016/11/4.
 */

public class TxtUtils {
    final static String HEX_DIGITS="0123456789ABCDEF";

    /**
     * xxxxxxxxxxxx ----->xx:xx:xx:xx:xx:xx
     * xxxx xxxx xxxx ---->  xx:xx:xx:xx:xx:xx
     *
     * @param mmc_id
     * @return
     */
    public static String convertAddress(String mmc_id) {
        StringBuilder sb = new StringBuilder();
        String new_id = clearSeparate(mmc_id);
        if (TextUtils.isEmpty(new_id)) {
            return null;
        }
        for (int i = 0; i < 12; i++) {
            sb.append(new_id.charAt(i));
            if (i % 2 == 1 && i != 11) {
                sb.append(":");
            }
        }
        return sb.toString();
    }


    /**
     * xx:xx:xx:xx:xx:xx ----> xxxx xxxx xxxx
     * xxxxxxxxxxxx-------->xxxx xxxx xxxx
     *
     * @param mmc_addr
     * @return
     */
    public static String convertAddressInverse(String mmc_addr) {
        try {

            StringBuilder sb = new StringBuilder(clearSeparate(mmc_addr));
            sb.insert(8, " ");
            sb.insert(4, " ");
            return sb.toString();
        } catch (Exception e) {
            return mmc_addr;
        }
    }


    /**
     * 清除空格和冒号
     *
     * @return
     */
    public static String clearSeparate(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String reg = "[\\s+]|[:]";
        return str.replaceAll(reg, "");
    }

    /**
     * check if like xxxx xxxx xxxx
     */
    public static boolean checkAddress(String macId) {
//        Log.e("TAG", "length:" + bleAddress.length());
        String regx = "[[0-9]|[a-f]|[A-F]]{4}\\s[[0-9]|[a-f]|[A-F]]{4}\\s[[0-9]|[a-f]|[A-F]]{4}";
        if (TextUtils.isEmpty(macId)) {
            return false;
        }
        if (macId.matches(regx)) {
            return true;
        }
        return false;
    }

    /**
     * 清除空格
     *
     * @param et
     * @return
     */
    public static String clearTextSpace(String et) {
        if (TextUtils.isEmpty(et)) {
            return "";
        }
        String txt = new String(et);
        String reg = "\\s+";
        txt = txt.replaceAll(reg, "");
        return txt;
    }

    /**
     * 格式化String,每隔 @length 个位置自动插入一个空格
     */
    public static String formatText(String et, int length) {
        String txt = clearTextSpace(et);
        StringBuilder sb = new StringBuilder(txt);
        for (int i = sb.length() - 1; i >= 0; i--) {
            if ((i + 1) % length == 0) {
                sb.insert(i + 1, ' ');
            }
        }
        return sb.toString();
    }

    //得到各个字符的16进制值
    private static byte charToByte(char c) {
        return (byte) HEX_DIGITS.indexOf(c);
    }


    /**
     * xx:xx:xx:xx:xx:xx ==> byte[]
     * http://blog.csdn.net/redhat456/article/details/4492310
     *
     * @param str
     * @return
     */
    public static byte[] strToBytes(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String reg = "[\\s+]|[:]";
        str = str.replaceAll(reg, "");
        str = str.toUpperCase();
        int len = str.length() / 2;
        char[] chars = str.toCharArray();

        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(chars[pos]) << 4 | charToByte(chars[pos + 1]));
        }
        return bytes;
    }

    public static String byteToHexString(byte[] array, int offset, int length,boolean split,boolean hexPrefix){
        int len=length*(2+(split?2:0)+(hexPrefix?2:0));
        char[] buf = new char[len];
        int bufIndex = 0;
        for (int i = offset; i < offset + length; i++) {
            if(split){
                buf[bufIndex++]=' ';
                buf[bufIndex++]=' ';
            }
            if(hexPrefix){
                buf[bufIndex++]='0';
                buf[bufIndex++]='x';
            }
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS.charAt((b >>> 4) & 0x0F);
            buf[bufIndex++] = HEX_DIGITS.charAt(b & 0x0F);
        }
        return new String(buf);
    }

}
