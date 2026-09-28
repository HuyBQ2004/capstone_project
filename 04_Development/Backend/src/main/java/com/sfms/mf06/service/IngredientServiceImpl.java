package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.unit.MeasureUnitService;
import com.sfms.mf06.domain.CatalogIngredient;
import com.sfms.mf06.domain.StorageZone;
import com.sfms.mf06.dto.IngredientDtos.CreateRequest;
import com.sfms.mf06.dto.IngredientDtos.IngredientResponse;
import com.sfms.mf06.dto.IngredientDtos.UpdateRequest;
import com.sfms.mf06.repository.CatalogIngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class IngredientServiceImpl implements IngredientService {

    private final CatalogIngredientRepository repository;
    private final CodeGenerator codeGenerator;
    private final MeasureUnitService measureUnitService;

    public IngredientServiceImpl(CatalogIngredientRepository repository, CodeGenerator codeGenerator,
                                  MeasureUnitService measureUnitService) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
        this.measureUnitService = measureUnitService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> list() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(IngredientServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional
    public IngredientResponse create(CreateRequest request) {
        // Tạo mã tự động nếu bỏ trống
        String code = (request.code() != null && !request.code().isBlank())
                ? request.code().trim().toUpperCase()
                : codeGenerator.next("mf06_ingredient", "ING", 3);

        if (repository.findByCode(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã nguyên liệu đã tồn tại: " + code);
        }

        String unit = java.text.Normalizer.normalize(request.unit().trim(), java.text.Normalizer.Form.NFC);
        // PROPOSED — xem plan thiết kế "Đơn vị đo lường": baseUnit phải nằm trong bảng
        // measure_units, không còn chấp nhận chuỗi bất kỳ (chặn lỗi gõ "Kg"/"KG" lẫn lộn).
        if (!measureUnitService.isValidUnit(unit)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "unit không hợp lệ: " + unit + " — phải nằm trong danh sách đơn vị đo lường hệ thống");
        }

        CatalogIngredient ing = new CatalogIngredient();
        ing.setCode(code);
        ing.setName(request.name().trim());
        ing.setUnit(unit);
        ing.setStorageZone(parseZone(request.storageZone()));
        ing.setRefuseFactor(request.refuseFactor() != null ? request.refuseFactor() : BigDecimal.ZERO);
        ing.setAllergens(request.allergens());
        ing.setActive(true);
        return toResponse(repository.saveAndFlush(ing));
    }

    @Override
    @Transactional
    public IngredientResponse update(String code, UpdateRequest request) {
        CatalogIngredient ing = repository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu: " + code));

        if (request.name() != null && !request.name().isBlank()) ing.setName(request.name().trim());
        if (request.unit() != null && !request.unit().isBlank()) {
            String unit = java.text.Normalizer.normalize(request.unit().trim(), java.text.Normalizer.Form.NFC);
            if (!measureUnitService.isValidUnit(unit)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "unit không hợp lệ: " + unit + " — phải nằm trong danh sách đơn vị đo lường hệ thống");
            }
            ing.setUnit(unit);
        }
        if (request.storageZone() != null) ing.setStorageZone(parseZone(request.storageZone()));
        if (request.refuseFactor() != null) ing.setRefuseFactor(request.refuseFactor());
        // `null` = giữ nguyên (PATCH bán phần); mảng rỗng = người dùng cố ý xoá hết tác nhân.
        if (request.allergens() != null) ing.setAllergens(request.allergens());
        if (request.active() != null) ing.setActive(request.active());
        return toResponse(repository.saveAndFlush(ing));
    }

    @Override
    @Transactional
    public void deactivate(String code) {
        CatalogIngredient ing = repository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nguyên liệu: " + code));
        ing.setActive(false);
        repository.save(ing);
    }

    private static StorageZone parseZone(String raw) {
        try {
            return StorageZone.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "storageZone không hợp lệ: " + raw + " (chấp nhận: DRY, CHILLED, FROZEN)");
        }
    }

    private static IngredientResponse toResponse(CatalogIngredient i) {
        return new IngredientResponse(i.getCode(), i.getName(), i.getUnit(), i.getStorageZone().name(),
                i.getRefuseFactor(), i.getAllergens());
    }
}
