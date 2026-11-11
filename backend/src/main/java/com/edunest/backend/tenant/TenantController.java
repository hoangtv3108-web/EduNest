package com.edunest.backend.tenant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class TenantController {

    private final TenantRegistrationService registrationService;
    private final TenantRepository repository;

    public TenantController(TenantRegistrationService registrationService, TenantRepository repository) {
        this.registrationService = registrationService;
        this.repository = repository;
    }

    /** Công khai (không cần token): chủ trung tâm đăng ký và nhận tài khoản quản trị. */
    @PostMapping("/public/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterTenantResponse dangKy(@Valid @RequestBody RegisterTenantRequest request) {
        return registrationService.dangKy(request);
    }

    /** Cần token. Không có WHERE nhưng chỉ trả trung tâm của người gọi, nhờ RLS. */
    @GetMapping("/tenant/current")
    public TenantInfo trungTamHienTai() {
        return repository.timTrungTamHienTai()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy trung tâm"));
    }
}
