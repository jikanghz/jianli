package com.jianli.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class ByteUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public byte[] read(InputStream inputStream, int length) throws Exception {
        byte[] bytes = new byte[0];
        while (true) {
            Thread.sleep(10);
            int stepLength = length - bytes.length;
            if (stepLength > 0) {
                byte[] stepBytes = new byte[stepLength];
                int count = inputStream.read(stepBytes);
                if (count < 0) {
                    continue;
                }
                bytes = merge(bytes, stepBytes, 0, count);
            }
            if (bytes.length >= length) {
                break;
            }
        }
        logger.debug("read:" + bytesToString16(bytes));
        return bytes;
    }


    public byte[] merge(byte[] a, byte[] b) {
        return merge(a, b, 0, b.length);
    }

    public byte[] merge(byte[] a, byte[] b, int begin, int end) {
        byte[] newBytes = null;
        if(a == null) {
            newBytes = new byte[end - begin];
            int i=0;
            for (int j = begin; j < end; j++, i++) {
                newBytes[i] = b[j];
            }
        }
        else {
            newBytes = new byte[a.length + end - begin];
            int i = 0;
            for (i = 0; i < a.length; i++) {
                newBytes[i] = a[i];
            }
            for (int j = begin; j < end; j++, i++) {
                newBytes[i] = b[j];
            }
        }
        return newBytes;
    }

    public byte[] subBytes(byte[] bytes, int offset, int length)
    {
        byte[] subBytes = new byte[length];
        for(int i=offset,index=0; i<bytes.length&&index<length; ++i, ++index)
        {
            subBytes[index] = bytes[i];
        }
        return subBytes;
    }

    public Integer bytes2ToInt(byte[] bytes)
    {
        return (bytes[0] & 0xff) << 8
                | (bytes[1] & 0xff);
    }

    public Integer bytes3ToInt(byte[] bytes) {
        return (bytes[0] & 0xff) << 16
                | (bytes[1] & 0xff) << 8
                | (bytes[2] & 0xff);
    }

    public Integer bytes4ToInt(byte[] bytes) {
        return (bytes[0] & 0xff) << 24
                | (bytes[1] & 0xff) << 16
                | (bytes[2] & 0xff) << 8
                | (bytes[3] & 0xff);
    }

    public String bytesToString16(byte[] b) {
        return bytesToString16(b, 0, b.length);
    }

    public String bytesToString16(byte[] b, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(String.format("%02x", b[offset + i]));
        }
        return stringBuilder.toString();
    }


    public String byteToString16(byte b){
        return String.format("%02x",b);
    }

    public String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }
}