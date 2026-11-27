package com.edunest.backend.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.springframework.web.multipart.MultipartFile;

public final class UploadGuard {
    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final byte[] ZIP_MAGIC = { 0x50, 0x4B, 0x03, 0x04 }; // .xlsx thực chất là zip

    private UploadGuard() {
    }

    public static void kiemTraExcel(MultipartFile f) {
        if (f == null || f.isEmpty())
            throw new IllegalArgumentException("File rỗng");
        if (f.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("File vượt quá 2MB");
        String ten = f.getOriginalFilename() == null ? "" : f.getOriginalFilename().toLowerCase();
        if (!ten.endsWith(".xlsx"))
            throw new IllegalArgumentException("Chỉ nhận file .xlsx");
        try (InputStream is = f.getInputStream()) {
            if (!Arrays.equals(is.readNBytes(4), ZIP_MAGIC))
                throw new IllegalArgumentException("Nội dung không phải file Excel (.xlsx) hợp lệ");
        } catch (IOException e) {
            throw new IllegalArgumentException("Không đọc được file");
        }
    }
}