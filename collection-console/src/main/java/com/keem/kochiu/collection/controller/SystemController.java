package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.bo.PathBo;
import com.keem.kochiu.collection.data.vo.ResourceTypeVo;
import com.keem.kochiu.collection.data.vo.StrategyVo;
import com.keem.kochiu.collection.enums.ResourceTypeEnum;
import com.keem.kochiu.collection.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.keem.kochiu.collection.Constant.PUBLIC_URL;

/**
 * @author KoChiu
 */
@RestController
@RequestMapping(PUBLIC_URL + "/sys")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
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

    @GetMapping("/strategy/list")
    public DefaultResult<List<StrategyVo>> getStrategyList() {
        return DefaultResult.ok(systemService.getStrategyList());
    }
}
