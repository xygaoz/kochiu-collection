package com.keem.kochiu.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.keem.kochiu.collection.data.bo.*;
import com.keem.kochiu.collection.data.dto.LoginDto;
import com.keem.kochiu.collection.data.dto.TokenDto;
import com.keem.kochiu.collection.data.vo.PageVo;
import com.keem.kochiu.collection.data.vo.RoleVo;
import com.keem.kochiu.collection.data.vo.UserVo;
import com.keem.kochiu.collection.entity.SysRole;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserPermission;
import com.keem.kochiu.collection.entity.UserRole;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.enums.PermitEnum;
import com.keem.kochiu.collection.enums.UserStatusEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.repository.SysSecurityRepository;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserPermissionRepository;
import com.keem.kochiu.collection.repository.UserRoleRepository;
import com.keem.kochiu.collection.util.HexUtils;
import com.keem.kochiu.collection.util.RsaHexUtil;
import com.keem.kochiu.collection.util.SHA256Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.keem.kochiu.collection.Constant.*;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.INVALID_USERNAME_OR_PASSWORD;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.SYS_ERROR;

@Slf4j
@Service
public class SysUserService {

    private final SysUserRepository userRepository;
    private final SysSecurityRepository securityRepository;
    private final TokenService tokenService;
    private final SysRoleService roleService;
    private final UserRoleRepository userRoleRepository;
    private final UserPermissionRepository userPermissionRepository;

    public SysUserService(SysUserRepository userRepository,
                          SysSecurityRepository securityRepository,
                          TokenService tokenService,
                          SysRoleService roleService,
                          UserRoleRepository userRoleRepository,
                          UserPermissionRepository userPermissionRepository) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
        this.tokenService = tokenService;
        this.roleService = roleService;
        this.userRoleRepository = userRoleRepository;
        this.userPermissionRepository = userPermissionRepository;
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
        return LoginDto.builder()
                .username(loginBo.getUsername())
                .token(tokenDto.getToken())
                .refreshToken(refreshToken)
                .expirySeconds(30 * 60)
                .build();
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
                                    .token(user.getToken())
                                    .key("*********")
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
                .key(RandomStringUtils.random(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"))
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
        if(StringUtils.isNotBlank(userInfoBo.getUserCode())){
            //判断用户编码是否被占用
            if(userRepository.getOneOpt(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserCode, userInfoBo.getUserCode()), false).isPresent()){
                throw new CollectionException(ErrorCodeEnum.USER_CODE_IS_EXIST);
            }
            user.setUserCode(userInfoBo.getUserCode());
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
    public void deleteUser(int userId) throws CollectionException {
        SysUser user = userRepository.getById(userId);
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }
        userRepository.removeById(userId);
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        userRoleRepository.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        userPermissionRepository.remove(new LambdaQueryWrapper<UserPermission>().eq(UserPermission::getUserId, userId));
    }

    //重置密码
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPwdBo resetPwdBo) throws CollectionException {
        SysUser user = userRepository.getById(resetPwdBo.getUserId());
        if(user == null){
            throw new CollectionException(ErrorCodeEnum.USER_IS_NOT_EXIST);
        }

        //密码解密
        String password = HexUtils.base64ToHex(resetPwdBo.getPassword());
        String confirmPassword = HexUtils.base64ToHex(resetPwdBo.getConfirmPassword());
        try {
            password = RsaHexUtil.decrypt(password, securityRepository.getPrivateKey());
            confirmPassword = RsaHexUtil.decrypt(confirmPassword, securityRepository.getPrivateKey());
        } catch (Exception e) {
            throw new CollectionException(ErrorCodeEnum.USER_PASSWORD_DECRYPT_ERROR);
        }

        if(!password.equals(confirmPassword)){
            throw new CollectionException(ErrorCodeEnum.PASSWORDS_ARE_INCONSISTENT_ERROR);
        }
        user.setPassword(SHA256Util.encryptBySHA256(password));
        userRepository.updateById(user);
    }

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
        if(userStatusEnum.getCode() != user.getStatus()) {
            user.setStatus(userStatusEnum.getCode());
            userRepository.updateById(user);
        }
    }
}
