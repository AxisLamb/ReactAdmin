package com.lain.modules.xianyu.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MessagePack 解码器（纯 Java 实现，对应 Python 版 MessagePackDecoder）
 */
public class MessagePackDecoder {

    private final byte[] data;
    private int pos;
    private final int length;

    public MessagePackDecoder(byte[] data) {
        this.data = data;
        this.pos = 0;
        this.length = data.length;
    }

    private int readByte() {
        if (pos >= length) {
            throw new IllegalArgumentException("Unexpected end of data");
        }
        return data[pos++] & 0xFF;
    }

    private byte[] readBytes(int count) {
        if (pos + count > length) {
            throw new IllegalArgumentException("Unexpected end of data");
        }
        byte[] result = new byte[count];
        System.arraycopy(data, pos, result, 0, count);
        pos += count;
        return result;
    }

    private int readUInt8() {
        return readByte();
    }

    private int readUInt16() {
        return ByteBuffer.wrap(readBytes(2)).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;
    }

    private long readUInt32() {
        return ByteBuffer.wrap(readBytes(4)).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    private long readUInt64() {
        return ByteBuffer.wrap(readBytes(8)).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    private int readInt8() {
        return ByteBuffer.wrap(readBytes(1)).order(ByteOrder.BIG_ENDIAN).get();
    }

    private int readInt16() {
        return ByteBuffer.wrap(readBytes(2)).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    private int readInt32() {
        return ByteBuffer.wrap(readBytes(4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private long readInt64() {
        return ByteBuffer.wrap(readBytes(8)).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    private float readFloat32() {
        return ByteBuffer.wrap(readBytes(4)).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    private double readFloat64() {
        return ByteBuffer.wrap(readBytes(8)).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    private String readString(int length) {
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    /**
     * 解码单个 MessagePack 值
     */
    public Object decodeValue() {
        if (pos >= length) {
            throw new IllegalArgumentException("Unexpected end of data");
        }

        int formatByte = readByte();

        // Positive fixint (0xxxxxxx)
        if (formatByte <= 0x7f) {
            return formatByte;
        }
        // Fixmap (1000xxxx)
        else if (formatByte >= 0x80 && formatByte <= 0x8f) {
            return decodeMap(formatByte & 0x0f);
        }
        // Fixarray (1001xxxx)
        else if (formatByte >= 0x90 && formatByte <= 0x9f) {
            return decodeArray(formatByte & 0x0f);
        }
        // Fixstr (101xxxxx)
        else if (formatByte >= 0xa0 && formatByte <= 0xbf) {
            return readString(formatByte & 0x1f);
        }
        // nil
        else if (formatByte == 0xc0) {
            return null;
        }
        // false
        else if (formatByte == 0xc2) {
            return false;
        }
        // true
        else if (formatByte == 0xc3) {
            return true;
        }
        // bin 8
        else if (formatByte == 0xc4) {
            return readBytes(readUInt8());
        }
        // bin 16
        else if (formatByte == 0xc5) {
            return readBytes(readUInt16());
        }
        // bin 32
        else if (formatByte == 0xc6) {
            return readBytes((int) readUInt32());
        }
        // float 32
        else if (formatByte == 0xca) {
            return readFloat32();
        }
        // float 64
        else if (formatByte == 0xcb) {
            return readFloat64();
        }
        // uint 8
        else if (formatByte == 0xcc) {
            return readUInt8();
        }
        // uint 16
        else if (formatByte == 0xcd) {
            return readUInt16();
        }
        // uint 32
        else if (formatByte == 0xce) {
            return readUInt32();
        }
        // uint 64
        else if (formatByte == 0xcf) {
            return readUInt64();
        }
        // int 8
        else if (formatByte == 0xd0) {
            return readInt8();
        }
        // int 16
        else if (formatByte == 0xd1) {
            return readInt16();
        }
        // int 32
        else if (formatByte == 0xd2) {
            return readInt32();
        }
        // int 64
        else if (formatByte == 0xd3) {
            return readInt64();
        }
        // str 8
        else if (formatByte == 0xd9) {
            return readString(readUInt8());
        }
        // str 16
        else if (formatByte == 0xda) {
            return readString(readUInt16());
        }
        // str 32
        else if (formatByte == 0xdb) {
            return readString((int) readUInt32());
        }
        // array 16
        else if (formatByte == 0xdc) {
            return decodeArray(readUInt16());
        }
        // array 32
        else if (formatByte == 0xdd) {
            return decodeArray((int) readUInt32());
        }
        // map 16
        else if (formatByte == 0xde) {
            return decodeMap(readUInt16());
        }
        // map 32
        else if (formatByte == 0xdf) {
            return decodeMap((int) readUInt32());
        }
        // Negative fixint (111xxxxx)
        else if (formatByte >= 0xe0) {
            return formatByte - 256;
        } else {
            throw new IllegalArgumentException(String.format("Unknown format byte: 0x%02x", formatByte));
        }
    }

    /**
     * 解码数组
     */
    public List<Object> decodeArray(int size) {
        List<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(decodeValue());
        }
        return result;
    }

    /**
     * 解码映射
     */
    public Map<Object, Object> decodeMap(int size) {
        Map<Object, Object> result = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            Object key = decodeValue();
            Object value = decodeValue();
            result.put(key, value);
        }
        return result;
    }

    /**
     * 解码 MessagePack 数据，失败时返回原始数据的 base64 编码
     */
    public Object decode() {
        try {
            return decodeValue();
        } catch (Exception e) {
            return Base64.getEncoder().encodeToString(data);
        }
    }
}
