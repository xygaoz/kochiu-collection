package com.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.kochiu.collection.data.bo.*;
import com.kochiu.collection.data.dto.LoginDto;
import com.kochiu.collection.data.dto.TokenDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.*;
import com.kochiu.collection.entity.*;
import com.kochiu.collection.enums.*;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.properties.UserConfigProperties;
import com.kochiu.collection.repository.*;
import com.kochiu.collection.service.store.ResourceStoreStrategy;
import com.kochiu.collection.service.store.ResourceStrategyFactory;
import com.kochiu.collection.util.HexUtils;
import com.kochiu.collection.util.RsaHexUtil;
import com.kochiu.collection.util.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.kochiu.collection.Constant.*;
import static com.kochiu.collection.enums.ErrorCodeEnum.INVALID_USERNAME_OR_PASSWORD;
import static com.kochiu.collection.enums.ErrorCodeEnum.SYS_ERROR;
import static com.kochiu.collection.util.DesensitizationUtil.mask;

@Slf4j
@Service
public class SysUserService {

    private final SysUserRepository userRepository;
    private final SysSecurityRepository securityRepository;
    private final TokenService tokenService;
    private final SysRoleService roleService;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final ResourceStrategyFactory resourceStrategyFactory;
    private final UserResourceRepository resourceRepository;
    private final UserCatalogRepository userCatalogRepository;
    private final UserTagRepository userTagRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final SysModuleService moduleService;
    private final UserConfigRepository userConfigRepository;
    private final UserConfigProperties userConfigProperties;

    public SysUserService(SysUserRepository userRepository,
                          SysSecurityRepository securityRepository,
                          TokenService tokenService,
                          SysRoleService roleService,
                          UserRoleRepository userRoleRepository,
                          UserPermissionRepository userPermissionRepository,
                          ResourceStrategyFactory resourceStrategyFactory,
                          UserResourceRepository resourceRepository,
                          UserCatalogRepository userCatalogRepository,
                          UserTagRepository userTagRepository,
                          UserCategoryRepository userCategoryRepository,
                          SysModuleService moduleService,
                          UserConfigRepository userConfigRepository,
                          UserConfigProperties userConfigProperties) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
        this.tokenService = tokenService;
        this.roleService = roleService;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.resourceRepository = resourceRepository;
        this.userCatalogRepository = userCatalogRepository;
        this.userTagRepository = userTagRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.moduleService = moduleService;
        this.userConfigRepository = userConfigRepository;
        this.userConfigProperties = userConfigProperties;
    }

    /**
     * 根据登录用户信息生成token，用户api操作
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    public TokenDto genToken(LoginBo loginBo) throws CollectionException {
        return genToken(loginBo, PermitEnum.API, -1);
    }

    /**
     * 根据登录用户信息生成token，用户ui操作, 默认30分钟超时
     * @param loginBo
     * @return
     * @throws CollectionException
     */
    public LoginDto login(LoginBo loginBo) throws CollectionException {
        //前端rsa加密是base64加密，所以解密时需要base64解密
        loginBo.setPassword(HexUtils.base64ToHex(loginBo.getPassword()));
        TokenDto tokenDto = genToken(loginBo, PermitEnum.UI, 30 * 60 * 1000);
        String refreshToken = tokenService.createToken(tokenDto.getUser(), tokenDto.getClaims(), 7 * 24 * 3600 * 1000);

        //获取用户权限
        Set<ModuleVo> permitModuleList = moduleService.getPermitModuleList(tokenDto.getUser().getUserId(), true);
        Set<String> permissions = new HashSet<>();
        for (ModuleVo moduleVo : permitModuleList) {
            listPermission(moduleVo, permissions);
        }

        return LoginDto.builder()
                .userId(tokenDto.getUser().getUserId())
                .userCode(loginBo.getUsername())
                .userName(loginBo.getNikeName())
                .token(tokenDto.getToken())
                .refreshToken(refreshToken)
                .expirySeconds(30 * 60)
                .permissions(permissions)
                .canDel(tokenDto.getUser().getCanDel())
                .build();
    }

    private void listPermission(ModuleVo moduleVo, Set<String> permissions) {

        if (moduleVo.getChildren() != null && !moduleVo.getChildren().isEmpty()) {
            for (ModuleVo child : moduleVo.getChildren()) {
                listPermission(child, permissions);
            }
        } else {
            if (moduleVo.getActions() != null && !moduleVo.getActions().isEmpty()) {
                for (ActionVo action : moduleVo.getActions()) {
                    permissions.add(moduleVo.getModuleCode() + ":" + action.getActionCode());
                }
            }
        }
    }

    /**
     * 根据登录用户信息生成token
     * @param loginBo
     * @return
     */
    private TokenDto genToken(LoginBo loginBo, PermitEnum permitEnum, long ttlMillis) throws CollectionException {

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUser::getUserCode, loginBo.getUsername());
        try {
            SysUser user = userRepository.getOne(lambdaQueryWrapper);
            if(user != null){
                loginBo.setNikeName(user.getUserName());
                String loginPwd = RsaHexUtil.decrypt(loginBo.getPassword(), securityRepository.getPrivateKey());
                loginPwd = SHA256Util.encryptBySHA256(loginPwd);
                if(loginPwd.equals(user.getPassword())){
                    Map<String, Object> claims = Map.of(
                            TOKEN_API_FLAG, permitEnum.name(),
                            TOKEN_TYPE_FLAG, TOKEN_TYPE_ACCESS
                    );
                    String token = tokenService.createToken(user,
                            claims,
                            ttlMillis);

                    if(ttlMillis == -1) {
                        //api访问时，token保存到数据库
                        user.setToken(token);
                    }
                    user.setUpdateBy(loginBo.getUsername());
                    userRepository.updateById(user);
                    return TokenDto.builder()
                            .user(user)
                            .token(token)
                            .claims(claims)
                            .build();
                }
                else{
                    throw new CollectionException(INVALID_USERNAME_OR_PASSWORD);
                }
            }
            else{
                throw new CollectionException(INVALID_USERNAME_OR_PASSWORD);
            }
        }
        catch (CollectionException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("系统错误", e);
            throw new CollectionException(SYS_ERROR);
        }
    }

    public void updateLastRefreshTime(SysUser user) {
        user.setLastTokenTime(LocalDateTime.now());
        userRepository.updateById(user);
    }

    //获取用户列表
    public PageVo<UserVo> listUsers(SearchUserBo searchUserBo) throws CollectionException {

        PageInfo<SysUser> userList = userRepository.listUser(searchUserBo);
        List<UserVo> users = userList.getList()
                .stream()
                .map(user -> {
                    List<SysRole> roles = roleService.selectUserRole(user.getUserId());
                            return UserVo.builder()
                                    .userId(user.getUserId())
                                    .userCode(user.getUserCode())
                                    .userName(user.getUserName())
                                    .status(user.getStatus())
                                    .token(user.getToken())
                                    .key("*********")
                                    .canDel(user.getCanDel())
                                    .strategy(user.getStrategy())
                                    .createTime(user.getCreateTime())
                                    .updateTime(user.getUpdateTime())
                                    .roles(roles.stream().map(role -> RoleVo.builder()
                                            .roleId(role.getRoleId())
                                            .roleName(role.getRoleName())
                                            .build()).toList())
                                    .build();
                        }
                ).toList();

        return PageVo.<UserVo>builder()
                .list(users)
                .pageNum(userList.getPageNum())
                .pageSize(userList.getPageSize())
                .total(userList.getTotal())
                .pages(userList.getPages())
                .build();
    }

    //添加用户
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserInfoBo userInfoBo) throws CollectionException {

        //判断用户编码是否被占用
        if(userRepository.getOneOpt(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserCode, userInfoBo.getUserCode()), false).isPresent()){
            throw new CollectionException(ErrorCodeEnum.USER_CODE_IS_EXIST);
        }

        //密码解密
        String password = HexUtils.base64ToHex(userInfoBo.getPassword());
        try {
            password = RsaHexUtil.decrypt(password, securityRepository.getPrivateKey());
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.USER_PASSWORD_DECRYPT_ERROR);
        }

        SysUser user = SysUser.builder()
                .userCode(userInfoBo.getUserCode())
                .userName(userInfoBo.getUserName())
                .password(SHA256Util.encryptBySHA256(password))
                .strategy(userInfoBo.getStrategy())
                .key(RandomStringUtils.random(8, RANDOM_CHARS))
                .build();
        userRepository.save(user);
        Integer userId = userRepository.getBaseMapper().selectLastInsertId();
        for(Integer roleId : userInfoBo.getRoles()){
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build();
            userRoleRepository.save(userRole);
        }
    }

    //更新用户
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserInfoBo userInfoBo) throws CollectionException {

        SysUser user = userRepository.getById(userInfoBo.getUserId());
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }
        if(StringUtils.isNotBlank(userInfoBo.getUserName())){
            user.setUserName(userInfoBo.getUserName());
        }
        if(StringUtils.isNotBlank(userInfoBo.getStrategy())){
            user.setStrategy(userInfoBo.getStrategy());
        }
        userRepository.updateById(user);
        //先删除后增加
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userInfoBo.getUserId()));
        for(Integer roleId : userInfoBo.getRoles()){
            UserRole userRole = UserRole.builder()
                    .userId(userInfoBo.getUserId())
                    .roleId(roleId)
                    .build();
            userRoleRepository.save(userRole);
        }
    }

    //删除用户
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(RemoveUserBo removeUserBo) throws CollectionException {
        SysUser user = userRepository.getById(removeUserBo.getUserId());
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }
        if(YesNoEnum.getEnum(user.getCanDel()) == YesNoEnum.NO){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_DELETE);
        }

        userRepository.removeById(user.getUserId());
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
        userPermissionRepository.deleteUserPermission(user.getUserId());

        if(removeUserBo.getDeleteOption() == RemoveUserOptionEnum.DELETE_RESOURCE){
            //删除用户资源
            ResourceStoreStrategy resourceStoreStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            List<UserResource> resources = resourceRepository.list(new LambdaQueryWrapper<UserResource>().eq(UserResource::getUserId, user.getUserId()));
            List<UserResource> rss = new ArrayList<>();
            resources.stream().filter(userResource ->
                    rss.add(userResource.clone())
            );
            resourceRepository.removeByIds(resources);
            //删除分类
            userCategoryRepository.remove(new LambdaQueryWrapper<UserCategory>().eq(UserCategory::getUserId, user.getUserId()));
            //删除目录
            userCatalogRepository.remove(new LambdaQueryWrapper<UserCatalog>().eq(UserCatalog::getUserId, user.getUserId()));
            //删除标签
            userTagRepository.remove(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, user.getUserId()));
            //删除文件
            for(UserResource userResource : rss){
                resourceStoreStrategy.deleteFile(user.getUserId(), userResource);
            }
        }
    }

    //重置密码
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(UserDto userDto, ResetPwdBo resetPwdBo) throws CollectionException {
        SysUser user = userRepository.getById(resetPwdBo.getUserId());
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }
        //自己不能重置自己的密码
        if(userDto.getUserId() == user.getUserId()){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_RESET_PASSWORD);
        }
        //不是管理员不能重置管理员密码
        SysUser me = userRepository.getById(userDto.getUserId());
        if(YesNoEnum.getEnum(me.getCanDel()) == YesNoEnum.YES && YesNoEnum.getEnum(user.getCanDel()) == YesNoEnum.NO){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_RESET_PASSWORD);
        }

        //密码解密
        String password = HexUtils.base64ToHex(resetPwdBo.getNewPassword());
        try {
            password = RsaHexUtil.decrypt(password, securityRepository.getPrivateKey());
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.USER_PASSWORD_DECRYPT_ERROR);
        }

        user.setPassword(SHA256Util.encryptBySHA256(password));
        userRepository.updateById(user);
    }

    //启用禁用用户
    @Transactional(rollbackFor = Exception.class)
    public void enableOrDisable(UserStatusBo userStatusBo) throws CollectionException {

        SysUser user = userRepository.getById(userStatusBo.getUserId());
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }

        UserStatusEnum userStatusEnum = UserStatusEnum.getByName(userStatusBo.getStatus());
        if(userStatusEnum == null){
            throw new CollectionException(ErrorCodeEnum.USER_STATUS_ERROR);
        }
        if(userStatusEnum == UserStatusEnum.STOP && YesNoEnum.getEnum(user.getCanDel()) == YesNoEnum.NO){
            throw new CollectionException(ErrorCodeEnum.USER_CANNOT_STOP);
        }

        if(userStatusEnum.getCode() != user.getStatus()) {
            user.setStatus(userStatusEnum.getCode());
            userRepository.updateById(user);
        }
    }

    //获取用户菜单
    public Set<MenuVo> getMyMenu(UserDto userDto) throws CollectionException {
        // 1. 获取用户信息
        SysUser user = userRepository.getUser(userDto);
        if (user == null) {
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }

        // 2. 获取用户有权限的模块
        List<SysModule> modules = userPermissionRepository.selectUserModule(user.getUserId());
        if (modules == null || modules.isEmpty()) {
            return new HashSet<>();
        }

        // 3. 构建模块映射表（以module_code为key）
        Map<String, SysModule> moduleMap = modules.stream()
                .collect(Collectors.toMap(SysModule::getModuleCode, m -> m));

        // 4. 构建父子关系映射
        Map<String, List<SysModule>> parentChildMap = new HashMap<>();
        for (SysModule module : modules) {
            String parentCode = getParentModuleCode(module.getModuleUrl());
            parentChildMap.computeIfAbsent(parentCode, k -> new ArrayList<>()).add(module);
        }

        // 5. 找出所有顶级菜单（没有父模块或父模块不在权限列表中的）
        Set<MenuVo> menuVos = new HashSet<>();
        for (SysModule module : modules) {
            String parentCode = getParentModuleCode(module.getModuleUrl());
            if (parentCode == null || !moduleMap.containsKey(parentCode)) {
                MenuVo menuVo = convertToMenuVo(module);
                buildMenuTree(menuVo, moduleMap, parentChildMap, new HashSet<>());
                menuVos.add(menuVo);
            }
        }

        return menuVos;
    }

    /**
     * 从模块路径中提取父模块code
     */
    private String getParentModuleCode(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String normalizedPath = path.replaceAll("^/+|/+$", "").toLowerCase();
        String[] parts = normalizedPath.split("/");
        if (parts.length > 1) {
            return parts[parts.length - 2]; // 返回倒数第二部分作为父模块code
        }
        return null;
    }

    /**
     * 构建菜单树
     */
    private void buildMenuTree(MenuVo currentMenu,
                               Map<String, SysModule> moduleMap,
                               Map<String, List<SysModule>> parentChildMap,
                               Set<String> processedCodes) {
        String currentCode = currentMenu.getName(); // name存储的是module_code
        if (processedCodes.contains(currentCode)) {
            return; // 防止循环引用
        }
        processedCodes.add(currentCode);

        // 获取当前模块的所有子模块
        List<SysModule> children = parentChildMap.getOrDefault(currentCode, Collections.emptyList());
        if (!children.isEmpty()) {
            List<MenuVo> childMenus = new ArrayList<>();
            for (SysModule child : children) {
                MenuVo childMenu = convertToMenuVo(child);
                buildMenuTree(childMenu, moduleMap, parentChildMap, new HashSet<>(processedCodes));
                childMenus.add(childMenu);
            }
            currentMenu.setChildren(childMenus);
        }
    }

    /**
     * 将SysModule转换为MenuVo
     */
    private MenuVo convertToMenuVo(SysModule module) {
        MenuVo menuVo = new MenuVo();
        menuVo.setName(module.getModuleCode()); // 使用module_code作为唯一标识
        menuVo.setPath(module.getModuleUrl());
        menuVo.setTitle(module.getModuleName());
        menuVo.setIcon(module.getIcon());
        menuVo.setIconType(module.getIconType());
        menuVo.setStyle(module.getStyle());
        menuVo.setRedirect(module.getRedirect());
        return menuVo;
    }

    // 获取用户信息
    public UserVo getMyInfo(UserDto userDto) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);

        List<SysRole> roles = roleService.selectUserRole(user.getUserId());
        return UserVo.builder()
                .userCode(user.getUserCode())
                .userName(user.getUserName())
                .token(user.getToken())
                .key(mask(user.getKey(), 2, user.getKey().length() - 3, '*'))
                .strategy(user.getStrategy())
                .status(user.getStatus())
                .roles(roles.stream().map(role -> RoleVo.builder()
                        .roleId(role.getRoleId())
                        .roleName(role.getRoleName())
                        .build()).toList())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDto userDto, EditMyNameBo userInfoBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        user.setUserName(userInfoBo.getUserName());
        userRepository.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetKey(UserDto userDto) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        user.setKey(RandomStringUtils.random(12, RANDOM_CHARS));
        userRepository.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetToken(UserDto userDto) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        Map<String, Object> claims = Map.of(
                TOKEN_API_FLAG, PermitEnum.API.name(),
                TOKEN_TYPE_FLAG, TOKEN_TYPE_ACCESS
        );
        String token = tokenService.createToken(user,
                claims,
                -1);
        user.setToken(token);
        userRepository.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(UserDto userDto, ModifyPwdBo modifyPwdBo) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }

        //密码解密
        String oldPassword = HexUtils.base64ToHex(modifyPwdBo.getOldPassword());
        try {
            oldPassword = RsaHexUtil.decrypt(oldPassword, securityRepository.getPrivateKey());
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.USER_PASSWORD_DECRYPT_ERROR);
        }
        String newPassword = HexUtils.base64ToHex(modifyPwdBo.getNewPassword());
        try {
            newPassword = RsaHexUtil.decrypt(newPassword, securityRepository.getPrivateKey());
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.USER_PASSWORD_DECRYPT_ERROR);
        }

        if(!SHA256Util.encryptBySHA256(oldPassword).equals(user.getPassword())){
            throw new CollectionException(ErrorCodeEnum.USER_OLD_PASSWORD_ERROR);
        }

        user.setPassword(SHA256Util.encryptBySHA256(newPassword));
        userRepository.updateById(user);
    }

    //  设置用户配置
    public void setMyConfig(UserDto userDto, UserConfigProperties.UserProperty property) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }

        UserConfigProperties.UserProperty userProperty = userConfigProperties.getUserProperty(user.getUserId());
        userProperty.setListCategoryNum(property.getListCategoryNum());
        userProperty.setListCategoryBy(property.getListCategoryBy());
        userProperty.setResourcePageSize(property.getResourcePageSize());
        userProperty.setListTagNum(property.getListTagNum());
        userProperty.setListTagBy(property.getListTagBy());
        userConfigProperties.setUserProperty(user.getUserId(), userProperty);

        //删除再保存
        userConfigRepository.remove(new LambdaQueryWrapper<UserConfig>().eq(UserConfig::getUserId, user.getUserId()));
        Map<String, String> configMap = userProperty.toMap();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            UserConfig userConfig = new UserConfig();
            userConfig.setUserId(user.getUserId());
            userConfig.setConfigKey(entry.getKey());
            userConfig.setConfigValue(entry.getValue());
            userConfigRepository.save(userConfig);
        }
    }
}
