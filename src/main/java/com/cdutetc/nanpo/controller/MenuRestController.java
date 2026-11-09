package com.cdutetc.nanpo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdutetc.nanpo.common.Result;
import com.cdutetc.nanpo.dto.MenuRequest;
import com.cdutetc.nanpo.service.MenuCrudService;
import com.cdutetc.nanpo.vo.MenuPageVO;
import com.cdutetc.nanpo.vo.MenuVO;

@RestController
@RequestMapping("/api/menus")
public class MenuRestController {
    private final MenuCrudService service;

    public MenuRestController(MenuCrudService service) {
        this.service = service;
    }

    @GetMapping
    public Result<MenuPageVO> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.list(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<MenuVO> detail(@PathVariable Long id) {
        return Result.success(service.get(id));
    }

    @PostMapping
    public ResponseEntity<Result<MenuVO>> create(@RequestBody MenuRequest request) {
        MenuVO created = service.create(request);
        return ResponseEntity.created(URI.create("/api/menus/" + created.getId()))
                .body(Result.success(created));
    }

    @PutMapping("/{id}")
    public Result<MenuVO> update(@PathVariable Long id, @RequestBody MenuRequest request) {
        return Result.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @DeleteMapping
    public Result<Integer> deleteBatch(@RequestParam List<Long> ids) {
        return Result.success(service.deleteBatch(ids));
    }
}
