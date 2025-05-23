package com.kochiu.collection.controller;

import com.kochiu.collection.annotation.CheckPermit;
import com.kochiu.collection.annotation.Module;
import com.kochiu.collection.data.DefaultResult;
import com.kochiu.collection.data.bo.PathBo;
import com.kochiu.collection.data.dto.StrategyDto;
import com.kochiu.collection.data.vo.KeyVo;
import com.kochiu.collection.data.vo.ResourceTypeVo;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.properties.SysConfigProperties;
import com.kochiu.collection.properties.UserConfigProperties;
import com.kochiu.collection.service.CheckPermitAspect;
import com.kochiu.collection.service.SysStrategyService;
import com.kochiu.collection.service.SystemService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kochiu.collection.Constant.PUBLIC_URL;

/**
 * @author KoChiu
 */
@RestController
@RequestMapping(PUBLIC_URL + "/sys")
public class SystemController {

    private final SystemService systemService;
    private final SysStrategyService strategyService;
    private final SysConfigProperties sysConfigProperties;

    public SystemController(SystemService systemService,
                            SysStrategyService strategyService,
                            SysConfigProperties sysConfigProperties) {
        this.systemService = systemService;
        this.strategyService = strategyService;
        this.sysConfigProperties = sysConfigProperties;
    }

    @GetMapping("/resourceTypes")
    public DefaultResult<List<ResourceTypeVo>> getSysResourceTypes() {

        List<ResourceTypeVo> resourceTypeVos = new ArrayList<>();
        Arrays.stream(ResourceTypeEnum.values()).forEach(resourceTypeEnum -> {
            resourceTypeVos.add(new ResourceTypeVo(resourceTypeEnum.getLabel(), resourceTypeEnum.name().toLowerCase()));
        });

        return new DefaultResult<>(resourceTypeVos);
    }

    @GetMapping("/resourceType/get/{type}")
    public DefaultResult<ResourceTypeVo> getSysResourceType(@PathVariable String type) {

        ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.valueOf(type.toUpperCase());
        ResourceTypeVo resourceTypeVo = new ResourceTypeVo(resourceTypeEnum.getLabel(), resourceTypeEnum.name().toLowerCase());

        return DefaultResult.ok(resourceTypeVo);
    }

    @PostMapping("/testServerPath")
    public DefaultResult<Boolean> testServerPath(PathBo pathBo) {
        return DefaultResult.ok(systemService.testServerPath(pathBo));
    }

    @CheckPermit
    @GetMapping("/strategy/list")
    public DefaultResult<List<StrategyDto>> getStrategyList() {
        return DefaultResult.ok(strategyService.getStrategyList());
    }

    @CheckPermit(modules = {
            @Module(modeCode = "strategy", byAction = {"update"})
    })
    @PostMapping("/strategy/update")
    public DefaultResult<Boolean> updateStrategy(@Validated StrategyDto strategyDto) throws CollectionException {
        strategyService.updateStrategy(strategyDto);
        return DefaultResult.ok(true);
    }

    @CheckPermit
    @GetMapping("/strategy/check-local")
    public DefaultResult<Boolean> checkLocalStrategy() {
        return DefaultResult.ok(strategyService.checkLocalStrategy());
    }

    @CheckPermit(modules = {
            @Module(modeCode = "config", byAction = {"clear"})
    })
    @GetMapping("/test/clear")
    public DefaultResult<Boolean> clearTest() throws CollectionException {
        systemService.clear();
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "config")
    })
    @GetMapping("/load-key")
    public DefaultResult<KeyVo> loadKeys() throws CollectionException {
        return DefaultResult.ok(systemService.getKeys());
    }

    @CheckPermit(modules = {
            @Module(modeCode = "config", byAction = {"reset-rsa-keys"})
    })
    @GetMapping("/reset-rsa-keys")
    public DefaultResult<Boolean> resetRsaKeys() throws CollectionException {
        systemService.resetRsaKeys();
        return DefaultResult.ok(true);
    }

    @CheckPermit(modules = {
            @Module(modeCode = "config", byAction = {"reset-key"})
    })
    @GetMapping("/reset-key")
    public DefaultResult<Boolean> resetCommonKey() throws CollectionException {
        systemService.resetCommonKey();
        return DefaultResult.ok(true);
    }

    @GetMapping("/sys-config")
    public DefaultResult<SysConfigProperties.SysProperty> getSysConfig() {
        return DefaultResult.ok(sysConfigProperties.getSysProperty());
    }

    @CheckPermit(modules = {
            @Module(modeCode = "config", byAction = {"set-sys-config"})
    })
    @PostMapping("/set-sys-config")
    public DefaultResult<Boolean> setSysConfig(@Validated SysConfigProperties.SysProperty property) {

        systemService.setSysConfig(property);
        return DefaultResult.ok(true);
    }
}
