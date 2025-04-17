package com.keem.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.CatalogVo;
import com.keem.kochiu.collection.data.vo.PathVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCatalogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCatalogService {

    private final UserCatalogRepository catalogRepository;
    private final SysUserRepository userRepository;

    public UserCatalogService(UserCatalogRepository catalogRepository,
                              SysUserRepository userRepository) {
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取用户目录
     * @param userDto
     * @return
     */
    public CatalogVo getCatalogTree(UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        List<UserCatalog> catalogList = getCatalogList(user.getUserId(), null);
        if(catalogList.size() != 1){
            throw new CollectionException(ErrorCodeEnum.ROOT_FOLDER_IS_INVALID);
        }

        // 构建根目录
        CatalogVo rootCatalogVo = new CatalogVo();
        rootCatalogVo.setLabel(catalogList.get(0).getCataName());
        rootCatalogVo.setId(catalogList.get(0).getCataSno());
        rootCatalogVo.setLevel(catalogList.get(0).getCataLevel());
        rootCatalogVo.setSno(catalogList.get(0).getCataSno());

        // 递归构建子目录
        buildCatalogTree(user.getUserId(), rootCatalogVo, catalogList.get(0).getCataId());

        return rootCatalogVo;
    }

    /**
     * 递归构建目录树
     * @param parentCatalogVo 父目录
     * @param parentId 父目录ID
     */
    private void buildCatalogTree(int userId, CatalogVo parentCatalogVo, Long parentId) {
        List<UserCatalog> children = getCatalogList(userId, parentId);
        for (UserCatalog child : children) {
            CatalogVo childCatalogVo = new CatalogVo();
            childCatalogVo.setLabel(child.getCataName());
            childCatalogVo.setId(child.getCataSno());
            childCatalogVo.setLevel(child.getCataLevel());
            childCatalogVo.setSno(child.getCataSno());
            parentCatalogVo.getChildren().add(childCatalogVo);
            // 递归处理子目录
            buildCatalogTree(userId, childCatalogVo, child.getCataId());
        }
    }

    private List<UserCatalog> getCatalogList(int userId, Long parentId) {
        LambdaQueryWrapper<UserCatalog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserCatalog::getUserId, userId);
        if(parentId == null){
            lambdaQueryWrapper.isNull(UserCatalog::getParentId);
        }
        else {
            lambdaQueryWrapper.eq(UserCatalog::getParentId, parentId);
        }
        lambdaQueryWrapper.orderByAsc(UserCatalog::getCataName);
        return catalogRepository.list(lambdaQueryWrapper);
    }

    /**
     * 获取目录路径
     * @param userDto
     * @param sno
     * @return
     * @throws CollectionException
     */
    public PathVo getCatalogPath(UserDto userDto, int sno) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataSno, sno));
        PathVo pathVo = new PathVo();
        if("/".equals(userCatalog.getCataPath())){
            pathVo.setPath("/我的资源");
        }
        else{
            pathVo.setPath("/我的资源" + userCatalog.getCataPath());
        }
        List<UserCatalog> catalogList = catalogRepository.selectParentCata(user.getUserId(), userCatalog.getCataId());
        catalogList.forEach(catalog ->
                pathVo.getPathInfo().add(new PathVo.PathInfo(catalog.getCataSno(), catalog.getCataName()))
        );

        return pathVo;
    }
}
