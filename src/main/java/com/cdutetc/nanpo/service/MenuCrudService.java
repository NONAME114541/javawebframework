package com.cdutetc.nanpo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdutetc.nanpo.common.BusinessException;
import com.cdutetc.nanpo.common.ErrorCodeEnum;
import com.cdutetc.nanpo.dao.MenuRepository;
import com.cdutetc.nanpo.dto.MenuRequest;
import com.cdutetc.nanpo.entity.Menu;
import com.cdutetc.nanpo.vo.MenuPageVO;
import com.cdutetc.nanpo.vo.MenuVO;

@Service
public class MenuCrudService {
    private final MenuRepository menus;

    public MenuCrudService(MenuRepository menus) {
        this.menus = menus;
    }

    @Transactional(readOnly = true)
    public MenuPageVO list(int page, int size, String keyword) {
        if (page < 0 || size < 1 || size > 50) invalid();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Menu> result = keyword == null || keyword.isBlank()
                ? menus.findAll(pageable)
                : menus.findByDishNameContainingIgnoreCase(keyword.trim(), pageable);
        return new MenuPageVO(result.getContent().stream().map(MenuVO::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public MenuVO get(Long id) {
        return MenuVO.from(require(id));
    }

    @Transactional
    public MenuVO create(MenuRequest request) {
        Menu menu = new Menu();
        apply(menu, request);
        return MenuVO.from(menus.saveAndFlush(menu));
    }

    @Transactional
    public MenuVO update(Long id, MenuRequest request) {
        Menu menu = require(id);
        apply(menu, request);
        return MenuVO.from(menus.saveAndFlush(menu));
    }

    @Transactional
    public void delete(Long id) {
        menus.delete(require(id));
    }

    @Transactional
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id < 1)) invalid();
        List<Long> distinct = ids.stream().distinct().toList();
        if (menus.findAllById(distinct).size() != distinct.size())
            throw new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND);
        menus.deleteAllByIdInBatch(distinct);
        return distinct.size();
    }

    private Menu require(Long id) {
        if (id == null || id < 1) invalid();
        return menus.findById(id).orElseThrow(() -> new BusinessException(ErrorCodeEnum.DISH_NOT_FOUND));
    }

    private void apply(Menu menu, MenuRequest request) {
        if (request == null || request.getDishName() == null || request.getDishName().isBlank()
                || request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            invalid();
        menu.setDishName(request.getDishName().trim());
        menu.setPrice(request.getPrice());
        menu.setImageUrl(request.getImageUrl());
    }

    private void invalid() {
        throw new BusinessException(ErrorCodeEnum.PARAMETER_INVALID);
    }
}
