package com.keem.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keem.kochiu.collection.data.bo.CatalogBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.CatalogVo;
import com.keem.kochiu.collection.data.vo.PathVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.properties.CollectionProperties;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCatalogRepository;
import com.keem.kochiu.collection.service.store.ResourceStoreStrategy;
import com.keem.kochiu.collection.service.store.ResourceStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class UserCatalogService {

    private final UserCatalogRepository catalogRepository;
    private final SysUserRepository userRepository;
    private final ResourceStrategyFactory resourceStrategyFactory;

    public UserCatalogService(UserCatalogRepository catalogRepository,
                              SysUserRepository userRepository,
                              ResourceStrategyFactory resourceStrategyFactory) {
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.resourceStrategyFactory = resourceStrategyFactory;
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
        rootCatalogVo.setId(catalogList.get(0).getCataId());
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
            childCatalogVo.setId(child.getCataId());
            childCatalogVo.setLevel(child.getCataLevel());
            childCatalogVo.setSno(child.getCataSno());
            childCatalogVo.setParentId(child.getParentId());
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

        PathVo pathVo = new PathVo();
        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataSno, sno));
        if(userCatalog == null){
            pathVo.setPath("/我的资源");
            return pathVo;
        }

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

    /**
     * 添加目录
     * @param userDto
     * @param catalogBo
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCatalog(UserDto userDto, CatalogBo catalogBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);

        UserCatalog parentCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, catalogBo.getParentId()));
        if(parentCatalog == null){
            throw new CollectionException(ErrorCodeEnum.PARENT_CATALOG_IS_INVALID);
        }
        if(parentCatalog.getCataLevel() + 1 > 3){
            throw new CollectionException(ErrorCodeEnum.CATALOG_LEVEL_IS_MAX);
        }

        //取最大序号
        Integer maxSno = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .orderByDesc(UserCatalog::getCataSno)
                .last("limit 1")).getCataSno();

        UserCatalog userCatalog = UserCatalog.builder()
                .userId(user.getUserId())
                .parentId(catalogBo.getParentId())
                .cataName(catalogBo.getCataName())
                .cataLevel(parentCatalog.getCataLevel() + 1)
                .cataPath((parentCatalog.getCataPath() + "/" + catalogBo.getCataName()).replaceAll("//", "/"))
                .cataSno(maxSno + 1)
                .build();
        if(catalogRepository.save(userCatalog)){
            //建物理文件夹
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            if(!resourceStoreStrategy.addFolder("/" + user.getUserCode() + userCatalog.getCataPath())){
                throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
            }
        }
        else{
            throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCatalog(UserDto userDto, CatalogBo catalogBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, catalogBo.getCateId()));
        if(userCatalog == null){
            throw new CollectionException(ErrorCodeEnum.CATALOG_IS_INVALID);
        }

        if(catalogBo.getParentId().equals(userCatalog.getParentId())){
            // 仅改名
            if(catalogBo.getCataName().equals(userCatalog.getCataName())){
                throw new CollectionException(ErrorCodeEnum.CATALOG_NAME_IS_SAME);
            }

            String oldPath = userCatalog.getCataPath();
            // 分割路径并替换最后一部分
            String[] pathParts = userCatalog.getCataPath().split("/");
            if (pathParts.length > 0) {
                pathParts[pathParts.length - 1] = catalogBo.getCataName();
            }
            String newPath = String.join("/", pathParts).replaceAll("//", "/");

            // 数据库先改名
            userCatalog.setCataName(catalogBo.getCataName());
            userCatalog.setCataPath(newPath);

            // 更新数据库
            if (!catalogRepository.updateById(userCatalog)) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }

            // 更新物理文件夹
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            if (!resourceStoreStrategy.renameFolder(
                    ("/" + user.getUserCode() + "/" + oldPath).replaceAll("//", "/"),
                    ("/" + user.getUserCode() + "/" + newPath).replaceAll("//", "/"),
                    true
            )) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }
        }
        else{
            //改名兼移动
            String oldPath = userCatalog.getCataPath();
            UserCatalog parentCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataId, catalogBo.getParentId()));
            if(parentCatalog == null){
                throw new CollectionException(ErrorCodeEnum.PARENT_CATALOG_IS_INVALID);
            }
            if(parentCatalog.getCataLevel() + 1 > 3){
                throw new CollectionException(ErrorCodeEnum.CATALOG_LEVEL_IS_MAX);
            }

            if(catalogBo.getCataName().equals(parentCatalog.getCataName())){
                throw new CollectionException(ErrorCodeEnum.CATALOG_NAME_IS_SAME);
            }
            String newPath = (parentCatalog.getCataPath() + "/" + catalogBo.getCataName()).replaceAll("//", "/");
            if(catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataPath, newPath)) != null){
                //目标目录已存在
                if(!catalogRepository.removeById(userCatalog.getCataId())){
                    throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
                }
            }
            else{
                // 数据库先改名
                userCatalog.setCataName(catalogBo.getCataName());
                userCatalog.setCataPath(newPath);
                userCatalog.setParentId(catalogBo.getParentId());
                // 更新数据库
                if (!catalogRepository.updateById(userCatalog)) {
                    throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
                }
            }

            // 更新物理文件夹
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            if (!resourceStoreStrategy.renameFolder(
                    ("/" + user.getUserCode() + "/" + oldPath).replaceAll("//", "/"),
                    ("/" + user.getUserCode() + "/" + newPath).replaceAll("//", "/"),
                    false
            )) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }

        }
    }
}
