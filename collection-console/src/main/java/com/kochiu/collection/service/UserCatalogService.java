package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kochiu.collection.data.bo.CatalogBo;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.CatalogVo;
import com.kochiu.collection.data.vo.PathVo;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.RemoveEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserCatalogRepository;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.store.ResourceStoreStrategy;
import com.kochiu.collection.service.store.ResourceStrategyFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.kochiu.collection.Constant.ROOT_CATALOG_SNO;
import static com.kochiu.collection.Constant.ROOT_PATH;
import static com.kochiu.collection.util.SysUtil.tidyPath;

@Service
public class UserCatalogService {

    private final UserCatalogRepository catalogRepository;
    private final SysUserRepository userRepository;
    private final ResourceStrategyFactory resourceStrategyFactory;
    private final UserResourceRepository resourceRepository;

    public UserCatalogService(UserCatalogRepository catalogRepository,
                              SysUserRepository userRepository,
                              ResourceStrategyFactory resourceStrategyFactory,
                              UserResourceRepository resourceRepository) {
        this.catalogRepository = catalogRepository;
        this.userRepository = userRepository;
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.resourceRepository = resourceRepository;
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

    public List<UserCatalog> getCatalogList(int userId, Long parentId) {
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
     * @param id
     * @return
     * @throws CollectionException
     */
    public PathVo getCatalogPath(UserDto userDto, int id) throws CollectionException {

        PathVo pathVo = new PathVo();
        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, id));
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
                pathVo.getPathInfo().add(new PathVo.PathInfo(catalog.getCataId(), catalog.getCataName()))
        );

        return pathVo;
    }

    /**
     * 添加目录
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addCatalog(UserDto userDto, CatalogBo catalogBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        return addCatalog(user, catalogBo);
    }

    /**
     * 添加目录
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addCatalog(SysUser user, CatalogBo catalogBo) throws CollectionException {

        UserCatalog parentCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, catalogBo.getParentId()));
        if(parentCatalog == null){
            throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_INVALID);
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
                .cataPath(tidyPath(parentCatalog.getCataPath() + "/" + catalogBo.getCataName()))
                .cataSno(maxSno + 1)
                .build();
        Long id = catalogRepository.insert(userCatalog);
        if(id != null){
            //建物理文件夹
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            if(!resourceStoreStrategy.addFolder("/" + user.getUserCode() + userCatalog.getCataPath())){
                throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
            }
            return id;
        }
        else{
            throw new CollectionException(ErrorCodeEnum.ADD_CATALOG_FAIL);
        }
    }

    /**
     * 根据路径添加，返回最终目录ID
     * @param userDto
     * @param path
     * @return
     * @throws CollectionException
     */
    public Long addCatalogPath(SysUser userDto, String path) throws CollectionException {

        if(path.startsWith("/")){
            path = path.substring(1);
        }
        String[] catalogNames = path.split("/");
        AtomicReference<Long> id = new AtomicReference<>();
        UserCatalog parent = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, userDto.getUserId())
                .eq(UserCatalog::getCataSno, ROOT_CATALOG_SNO));
        if(parent == null){
            throw new CollectionException(ErrorCodeEnum.CATALOG_NAME_IS_SAME);
        }
        Long parentId = parent.getCataId();

        String cataPath = "";
        for(String catalogName : catalogNames){
            cataPath += "/" + catalogName;
            UserCatalog catalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, userDto.getUserId())
                    .eq(UserCatalog::getCataPath, tidyPath(cataPath))
            );
            if(catalog == null){
                CatalogBo catalogBo = CatalogBo.builder()
                        .parentId(parentId)
                        .cataName(catalogName)
                        .build();
                id.set(addCatalog(userDto, catalogBo));
            }
            else{
                id.set(catalog.getCataId());
            }
            parentId = id.get();
        }
        return id.get();
    }

    //添加目录路径
    public Long addCatalogPath(SysUser user, Long parentCataId, String path) throws CollectionException {
        // 去除路径开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] catalogNames = path.split("/");

        // 获取父目录信息
        UserCatalog parent = getParentCatalog(user, parentCataId);
        if (parent == null) {
            throw new CollectionException(ErrorCodeEnum.CATALOG_NAME_IS_SAME);
        }

        // 获取父目录的当前深度
        int parentDepth = calculateCatalogDepth(parent.getCataPath());

        Long currentParentId = parent.getCataId();
        String currentPath = parent.getCataPath();
        Long lastValidId = currentParentId; // 记录最后一个有效ID（第4层目录）

        for (String catalogName : catalogNames) {
            // 计算新目录的深度
            int newDepth = parentDepth + 1;

            // 检查是否超过4层限制
            if (newDepth > 3) {
                // 查找是否已存在该路径的目录
                UserCatalog existingCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                        .eq(UserCatalog::getUserId, user.getUserId())
                        .eq(UserCatalog::getCataPath, tidyPath(currentPath + "/" + catalogName)));

                return existingCatalog != null ? existingCatalog.getCataId() : lastValidId;
            }

            // 构建新目录路径
            currentPath += "/" + catalogName;

            // 检查目录是否已存在
            UserCatalog catalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataPath, tidyPath(currentPath)));

            if (catalog == null) {
                // 创建新目录
                CatalogBo catalogBo = CatalogBo.builder()
                        .parentId(currentParentId)
                        .cataName(catalogName)
                        .build();
                Long newCatalogId = addCatalog(user, catalogBo);
                currentParentId = newCatalogId;
            } else {
                currentParentId = catalog.getCataId();
            }

            // 更新最后有效的目录ID（当达到第4层时记录）
            if (newDepth == 4) {
                lastValidId = currentParentId;
            }

            parentDepth = newDepth;
        }

        return currentParentId;
    }

    // 获取父目录信息
    public UserCatalog getParentCatalog(SysUser user, Long parentCataId) throws CollectionException {
        if (parentCataId != null) {
            UserCatalog parent = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataId, parentCataId));
            if (parent != null) {
                return parent;
            }
        }

        // 如果未提供父ID或父目录不存在，使用默认根目录
        return catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataSno, ROOT_CATALOG_SNO));
    }

    // 计算目录深度（根据路径中的斜杠数量）
    private int calculateCatalogDepth(String cataPath) {
        if (cataPath == null || cataPath.isEmpty() || ROOT_PATH.equals(cataPath)) {
            return 0;
        }
        if(cataPath.startsWith("/")){
            cataPath = cataPath.substring(1);
        }
        if(cataPath.endsWith("/")){
            cataPath = cataPath.substring(0, cataPath.length() - 1);
        }
        return StringUtils.countMatches(tidyPath(cataPath), "/") + 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCatalog(UserDto userDto, CatalogBo catalogBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, catalogBo.getCataId()));
        if(userCatalog == null){
            throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
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
            String newPath = tidyPath(String.join("/", pathParts));

            // 数据库先改名
            userCatalog.setCataName(catalogBo.getCataName());
            userCatalog.setCataPath(newPath);

            // 更新数据库
            if (!catalogRepository.updateById(userCatalog)) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }

            // 更新物理文件夹
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            //更新数据库
            resourceStoreStrategy.updateResourcePath(user.getUserId(), catalogBo.getCataId(), catalogBo.getCataId());
            if (!resourceStoreStrategy.renameFolder(
                    tidyPath("/" + user.getUserCode() + "/" + oldPath),
                    tidyPath("/" + user.getUserCode() + "/" + newPath),
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
                throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_INVALID);
            }
            if(parentCatalog.getCataLevel() + 1 > 3){
                throw new CollectionException(ErrorCodeEnum.CATALOG_LEVEL_IS_MAX);
            }

            if(catalogBo.getCataName().equals(parentCatalog.getCataName())){
                throw new CollectionException(ErrorCodeEnum.CATALOG_NAME_IS_SAME);
            }
            String newPath = tidyPath(parentCatalog.getCataPath() + "/" + catalogBo.getCataName());
            Long targetCataId = catalogBo.getParentId();
            if(catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataPath, newPath)) != null){
                //目标目录已存在，抛弃旧目录
                if(!catalogRepository.removeById(userCatalog.getCataId())){
                    throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
                }
            }
            else{
                //不存在，改当前目录的名称和路径
                userCatalog.setCataName(catalogBo.getCataName());
                userCatalog.setCataPath(newPath);
                userCatalog.setParentId(catalogBo.getParentId());
                if (!catalogRepository.updateById(userCatalog)) {
                    throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
                }
                targetCataId = userCatalog.getCataId();
            }

            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            //更新数据库
            resourceStoreStrategy.updateResourcePath(user.getUserId(), targetCataId, catalogBo.getCataId());
            // 更新物理文件夹
            if (!resourceStoreStrategy.renameFolder(
                    tidyPath("/" + user.getUserCode() + "/" + oldPath),
                    tidyPath("/" + user.getUserCode() + "/" + newPath),
                    false
            )) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }

        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCatalog(UserDto userDto, CatalogBo catalogBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        UserCatalog userCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                .eq(UserCatalog::getUserId, user.getUserId())
                .eq(UserCatalog::getCataId, catalogBo.getCataId()));
        if(userCatalog == null){
            throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
        }

        //直接删除
        if(catalogBo.getRemoveType() == RemoveEnum.REMOVE_TYPE_DELETE){
            String oldPath = userCatalog.getCataPath();
            //先删除目录
            if(!catalogRepository.removeById(userCatalog.getCataId())){
                throw new CollectionException(ErrorCodeEnum.REMOVE_CATALOG_FAIL);
            }
            //删除资源
            if(!resourceRepository.remove(new LambdaQueryWrapper<UserResource>()
                    .eq(UserResource::getUserId, user.getUserId())
                    .eq(UserResource::getCataId, userCatalog.getCataId())
            )){
                throw new CollectionException(ErrorCodeEnum.REMOVE_CATALOG_FAIL);
            }

            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            if(!resourceStoreStrategy.deleteFolder(tidyPath("/" + user.getUserCode() + "/" + oldPath))){
                throw new CollectionException(ErrorCodeEnum.REMOVE_CATALOG_FAIL);
            }
        }
        else {
            if(catalogBo.getParentId() == null){
                throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_NOT_NULL);
            }

            //删除前转移文件
            UserCatalog parentCatalog = catalogRepository.getOne(new LambdaQueryWrapper<UserCatalog>()
                    .eq(UserCatalog::getUserId, user.getUserId())
                    .eq(UserCatalog::getCataId, catalogBo.getParentId()));
            if (parentCatalog == null) {
                throw new CollectionException(ErrorCodeEnum.TARGET_CATALOG_IS_INVALID);
            }

            //先删除目录
            if(!catalogRepository.removeById(userCatalog.getCataId())){
                throw new CollectionException(ErrorCodeEnum.REMOVE_CATALOG_FAIL);
            }

            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            //更新数据库
            resourceStoreStrategy.updateResourcePath(user.getUserId(), catalogBo.getParentId(), catalogBo.getCataId());
            // 更新物理文件夹
            if (!resourceStoreStrategy.renameFolder(
                    tidyPath("/" + user.getUserCode() + "/" + userCatalog.getCataPath()),
                    tidyPath("/" + user.getUserCode() + "/" + parentCatalog.getCataPath()),
                    false
            )) {
                throw new CollectionException(ErrorCodeEnum.UPDATE_CATALOG_FAIL);
            }
        }
    }
}
