package com.edunest.backend.audit;

import java.util.List;

public record TrangNhatKy(List<NhatKyResponse> items, int page, int size, long total) {
}