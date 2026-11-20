package com.edunest.backend.xuat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Một file xuất ra: nội dung, tên gợi ý, loại nội dung và mã băm SHA-256 để đối
 * chiếu toàn vẹn.
 */
public record XuatFile(byte[] noiDung, String tenFile, String contentType, String sha256) {

    public static XuatFile tao(byte[] noiDung, String tenFile, String contentType) {
        return new XuatFile(noiDung, tenFile, contentType, sha256Hex(noiDung));
    }

    static String sha256Hex(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM không hỗ trợ SHA-256", e);
        }
    }
}