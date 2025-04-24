package com.keem.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ModuleVo {

    Integer moduleId;
    String moduleName;
    String moduleCode;
    List<ModuleVo> children;
    List<ActionVo> actions;
}
