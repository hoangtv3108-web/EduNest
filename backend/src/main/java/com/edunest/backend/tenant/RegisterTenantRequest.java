package com.edunest.backend.tenant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(
        @NotBlank(message = "Tên trung tâm không được để trống")
        @Size(max = 200, message = "Tên trung tâm tối đa 200 ký tự")
        String tenTrungTam,

        @NotBlank(message = "Email quản trị không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 255)
        String emailQuanTri,

        @NotBlank(message = "Họ tên quản trị không được để trống")
        @Size(max = 255)
        String hoTenQuanTri,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 128, message = "Mật khẩu từ 8 đến 128 ký tự")
        String matKhau) {}
