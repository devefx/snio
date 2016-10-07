package org.devefx.snio.util;

import java.io.ByteArrayOutputStream;

public final class HexUtils {
    public static final int[] DEC = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private static StringManager sm = StringManager.getManager("org.devefx.snio.util");

    public HexUtils() {
    }

    public static byte[] convert(String digits) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for(int i = 0; i < digits.length(); i += 2) {
            char c1 = digits.charAt(i);
            if(i + 1 >= digits.length()) {
                throw new IllegalArgumentException(sm.getString("hexUtil.odd"));
            }

            char c2 = digits.charAt(i + 1);
            byte b = 0;
            byte b1;
            if(c1 >= 48 && c1 <= 57) {
                b1 = (byte)(b + (c1 - 48) * 16);
            } else if(c1 >= 97 && c1 <= 102) {
                b1 = (byte)(b + (c1 - 97 + 10) * 16);
            } else {
                if(c1 < 65 || c1 > 70) {
                    throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
                }

                b1 = (byte)(b + (c1 - 65 + 10) * 16);
            }

            if(c2 >= 48 && c2 <= 57) {
                b1 = (byte)(b1 + (c2 - 48));
            } else if(c2 >= 97 && c2 <= 102) {
                b1 = (byte)(b1 + c2 - 97 + 10);
            } else {
                if(c2 < 65 || c2 > 70) {
                    throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
                }

                b1 = (byte)(b1 + c2 - 65 + 10);
            }

            baos.write(b1);
        }

        return baos.toByteArray();
    }

    public static String convert(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        for(int i = 0; i < bytes.length; ++i) {
            sb.append(convertDigit(bytes[i] >> 4));
            sb.append(convertDigit(bytes[i] & 15));
        }

        return sb.toString();
    }

    public static int convert2Int(byte[] hex) {
        if(hex.length < 4) {
            return 0;
        } else if(DEC[hex[0]] < 0) {
            throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
        } else {
            int len = DEC[hex[0]];
            len <<= 4;
            if(DEC[hex[1]] < 0) {
                throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
            } else {
                len += DEC[hex[1]];
                len <<= 4;
                if(DEC[hex[2]] < 0) {
                    throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
                } else {
                    len += DEC[hex[2]];
                    len <<= 4;
                    if(DEC[hex[3]] < 0) {
                        throw new IllegalArgumentException(sm.getString("hexUtil.bad"));
                    } else {
                        len += DEC[hex[3]];
                        return len;
                    }
                }
            }
        }
    }

    private static char convertDigit(int value) {
        value &= 15;
        return value >= 10?(char)(value - 10 + 97):(char)(value + 48);
    }
}