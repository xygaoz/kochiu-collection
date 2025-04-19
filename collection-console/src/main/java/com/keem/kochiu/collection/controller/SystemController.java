package com.keem.kochiu.collection.controller;

import com.keem.kochiu.collection.data.DefaultResult;
import com.keem.kochiu.collection.data.vo.ResourceTypeVo;
import com.keem.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
