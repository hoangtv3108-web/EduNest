package com.edunest.backend.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadGuardTest {
    private static MockMultipartFile f(String name, byte[] data) {
        return new MockMultipartFile("file", name, "application/octet-stream", data);
    }

    @Test
    void chapNhanXlsxHopLe() {
        assertDoesNotThrow(() -> UploadGuard.kiemTraExcel(f("hv.xlsx", new byte[] { 0x50, 0x4B, 0x03, 0x04, 9 })));
    }

    @Test
    void chanFileExeDoiDuoi() {
        assertThrows(IllegalArgumentException.class,
                () -> UploadGuard.kiemTraExcel(f("virus.exe", new byte[] { 1, 2, 3, 4 })));
    }

    @Test
    void chanFileGiaMaoXlsx() {
        assertThrows(IllegalArgumentException.class,
                () -> UploadGuard.kiemTraExcel(f("fake.xlsx", "MZ...".getBytes())));
    }

    @Test
    void chanFileQuaLon() {
        assertThrows(IllegalArgumentException.class,
                () -> UploadGuard.kiemTraExcel(f("big.xlsx", new byte[3 * 1024 * 1024])));
    }
}