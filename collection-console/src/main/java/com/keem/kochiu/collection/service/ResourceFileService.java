package com.keem.kochiu.collection.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keem.kochiu.collection.data.bo.BatchImportBo;
import com.keem.kochiu.collection.data.bo.PathBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
import com.keem.kochiu.collection.enums.FileTypeEnum;
import com.keem.kochiu.collection.enums.ImportMethodEnum;
import com.keem.kochiu.collection.enums.SaveTypeEnum;
import com.keem.kochiu.collection.exception.CollectionException;
import com.keem.kochiu.collection.handler.ImportProgressWebSocketHandler;
import com.keem.kochiu.collection.repository.SysUserRepository;
import com.keem.kochiu.collection.repository.UserCatalogRepository;
import com.keem.kochiu.collection.repository.UserResourceRepository;
import com.keem.kochiu.collection.service.store.ResourceStoreStrategy;
import com.keem.kochiu.collection.service.store.ResourceStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.keem.kochiu.collection.enums.ErrorCodeEnum.FILE_IS_EXIST;
import static com.keem.kochiu.collection.enums.ErrorCodeEnum.FILE_SAVING_FAILURE;

@Slf4j
@Service
public class ResourceFileService {

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;
    private final UserCatalogService userCatalogService;
    private final UserCatalogRepository catalogRepository;
    private final SystemService systemService;
    private final ObjectMapper objectMapper;

    public ResourceFileService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               UserCatalogService userCatalogService,
                               UserCatalogRepository catalogRepository,
                               SystemService systemService, ObjectMapper objectMapper) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.userCatalogService = userCatalogService;
        this.catalogRepository = catalogRepository;
        this.systemService = systemService;
        this.objectMapper = objectMapper;
    }

    /**
     * 通过策略保存文件
     * @param uploadBo
     * @return
     * @throws CollectionException
     */
    @Transactional(rollbackFor = Exception.class)
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("upload-", ".tmp");
            Files.copy(uploadBo.getFile().getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            // 计算MD5
            String md5 = DigestUtil.md5Hex(tempFile.toFile());

            // 使用临时文件
            try (InputStream is = Files.newInputStream(tempFile)) {
                return saveFile(is,
                        uploadBo.getFile().getOriginalFilename(),
                        md5,
                        userDto,
                        uploadBo.getCategoryId(),
                        uploadBo.getCataId(),
                        uploadBo.isOverwrite(),
                        uploadBo.isAutoCreate(),
                        user.getStrategy());
            } catch (IOException e) {
                log.error("文件保存失败", e);
                throw new CollectionException(FILE_SAVING_FAILURE);
            }
            finally {
                try {
                    Files.deleteIfExists(tempFile);
                }
                catch (IOException e) {
                    log.error("删除临时文件失败", e);
                }
            }
        }
        catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException(FILE_SAVING_FAILURE);
        }
    }

    /**
     * 保存文件
     * @param inputStream
     * @param originalFilename
     * @param userDto
     * @param categoryId
     * @param cataId
     * @param isOverwrite
     * @param isAutoCreate
     * @param strategy
     * @return
     * @throws CollectionException
     */
    protected FileVo saveFile(InputStream inputStream,
                              String originalFilename,
                              String md5,
                              UserDto userDto,
                              Long categoryId,
                              Long cataId,
                              boolean isOverwrite,
                              boolean isAutoCreate,
                              String strategy
                              ) throws CollectionException {
        List<UserResource> resources =resourceRepository.countFileMd5(userDto.getUserId(), md5);
        if(!resources.isEmpty() && !isOverwrite){
            throw new CollectionException(FILE_IS_EXIST);
        }

        UserCatalog catalog = catalogRepository.getById(cataId);
        if(catalog == null){
            throw new CollectionException(ErrorCodeEnum.CATEGORY_NOT_EXIST);
        }
        if(!isAutoCreate && (cataId == null || catalogRepository.getById(cataId) == null)){
            //非自动创建目录，检查目录是否存在
            throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
        }

        String savePath;
        if(isAutoCreate) {
            String catalogPath = ResourceStoreStrategy.dateFormat.format(System.currentTimeMillis());
            savePath = "/" + userDto.getUserCode() + "/" + catalogPath;
            //创建目录
            Long id = userCatalogService.addCatalogPath(userDto, catalogPath);
            if(id == null){
                throw new CollectionException(ErrorCodeEnum.CATALOG_CREATE_FAILURE);
            }
            else{
                cataId = id;
            }
        }
        else{
            savePath = "/" + userDto.getUserCode() + "/" + catalog.getCataPath();
        }
        savePath = savePath.replaceAll("//", "/");
        if(savePath.endsWith("/")){
            savePath = savePath.substring(0, savePath.length() - 1);
        }

        ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(strategy);
        FileVo fileVo = storeStrategy.saveFile(inputStream,
                        originalFilename,
                        userDto, md5, savePath, categoryId, cataId);
        //保存成功。删除原有记录
        if(!resources.isEmpty() && isOverwrite){
            for(UserResource resource : resources){
                resourceRepository.removeById(resource);
                //删除文件
                storeStrategy.deleteFile(userDto.getUserId(), resource);
            }
        }
        return fileVo;
    }

    /**
     * 通过策略下载文件
     * @param request
     * @param response
     * @param resourceId
     */
    public void download(HttpServletRequest request, HttpServletResponse response, Long resourceId) {

        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            response.setStatus(404);
            return;
        }

        int userId = resource.getUserId();
        SysUser user = userRepository.getById(userId);
        if (user == null) {
            response.setStatus(404);
            return;
        }

        resourceStrategyFactory.getStrategy(user.getStrategy()).download(request, response, resourceId);
    }

    /**
     * 批量导入
     * @param taskId
     * @param userDto
     * @param batchImportBo
     * @throws CollectionException
     */
    /**
     * 批量导入
     * @param taskId
     * @param userDto
     * @param batchImportBo
     * @throws CollectionException
     */
    public void batchImport(String taskId, UserDto userDto, BatchImportBo batchImportBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if (!systemService.testServerPath(PathBo.builder()
                .path(batchImportBo.getSourcePath())
                .importMethod(batchImportBo.getImportMethod())
                .build())) {
            throw new CollectionException(ErrorCodeEnum.SERVER_PATH_ERROR);
        }

        // 取出服务器路径里适合导入的文件
        List<File> files = listImportableFiles(batchImportBo.getSourcePath(), true);
        // 取用户根目录ID
        Long rootCataId = catalogRepository.getUserRoot(user.getUserId());
        if (rootCataId == null){
            throw new CollectionException(ErrorCodeEnum.ROOT_CATALOG_IS_INVALID);
        }
        // 判断目录是否存在
        if(batchImportBo.getCatalogId() != null){
            if(catalogRepository.getById(batchImportBo.getCatalogId()) == null){
                throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
            }
        }

        int successCount = 0, failCount = 0;
        try {
            for (int i = 0; i < files.size(); i++) {
                // 检查是否被取消（通过线程中断）
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("任务被取消");
                }

                File file = files.get(i);
                String filePath = file.getAbsolutePath();
                String relativePath = filePath.substring(batchImportBo.getSourcePath().length());

                // 获取文件MD5，查出相同文件的记录
                String md5 = DigestUtil.md5Hex(file);
                List<UserResource> resources = resourceRepository.countFileMd5(userDto.getUserId(), md5);

                ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());

                switch (batchImportBo.getImportMethod()){
                    case COPY:
                    case MOVE: {
                        if(handleCopyOrMove(batchImportBo, file, md5, userDto, user, rootCataId, resources, storeStrategy)){
                            successCount++;
                        }
                        else{
                            failCount++;
                        }

                        if(batchImportBo.getImportMethod() == ImportMethodEnum.MOVE){
                            Files.deleteIfExists(file.toPath());
                        }
                        break;
                    }
                    case KEEP_ORIGINAL: {
                        try {
                            // 提取子目录
                            String catalogPath = getSubPath(relativePath);
                            // 调用 saveLinkResource 方法保存链接资源
                            storeStrategy.saveLinkResource(file,
                                    userDto,
                                    md5,
                                    catalogPath, // 根据原目录结构保存缩略图
                                    batchImportBo.getCategoryId(),
                                    rootCataId
                            );
                            successCount++;
                        } catch (Exception e) {
                            log.error("文件保存失败", e);
                            failCount++;
                        }
                        break;
                    }
                    default:
                        throw new CollectionException(ErrorCodeEnum.IMPORT_METHOD_ERROR);
                }
                log.info("处理进度：{}%", (i+1)/(float)files.size()*100);

                // 发送进度前打印日志
                log.info("发送进度: taskId={}, progress={}/{}", taskId, i + 1, files.size());
                ImportProgressWebSocketHandler.ImportProgress progress = new ImportProgressWebSocketHandler.ImportProgress(i + 1, files.size(), successCount, failCount, filePath, "processing", null);
                String json = objectMapper.writeValueAsString(progress); // 假设使用 Jackson
                log.info("准备发送进度: {}", json); // 关键日志
                ImportProgressWebSocketHandler.sendProgress(taskId, progress);
            }
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(files.size(), files.size(), successCount, failCount, "", "completed", null)
            );
        } catch (InterruptedException e) {
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(-1, files.size(), successCount, failCount, "", "cancelled", "用户主动取消")
            );
            throw new CollectionException(ErrorCodeEnum.TASK_CANCELLED);
        } catch (CollectionException e) {
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(-1, files.size(), successCount, failCount, "", "error", e.getMessage())
            );
            throw e;
        } catch (Exception e) {
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(-1, files.size(), successCount, failCount, "", "error", "系统错误")
            );
            log.error("批量导入失败", e);
            throw new CollectionException(ErrorCodeEnum.IMPORT_ERROR, "系统错误");
        }
    }

    /**
     * 处理复制或移动
     */
    private boolean handleCopyOrMove(BatchImportBo batchImportBo, File file,
                                  String md5, UserDto userDto, SysUser user,
                                  Long rootCataId, List<UserResource> resources,
                                  ResourceStoreStrategy storeStrategy) throws CollectionException {

        //假如没有选择导入目录，下面的目录规则生效
        if(batchImportBo.getCatalogId() == null) {
            switch (batchImportBo.getAutoCreateRule()) {
                //在根目录创建日期目录
                case CREATE_DATE_DIRECTORY: {
                    try (InputStream is = new FileInputStream(file)) {
                        saveFile(is,
                                file.getName(),
                                md5,
                                userDto,
                                batchImportBo.getCategoryId(),
                                rootCataId,
                                true,
                                true,
                                user.getStrategy());

                        //保存成功。删除原有记录
                        if (!resources.isEmpty()) {
                            for (UserResource resource : resources) {
                                resourceRepository.removeById(resource);
                                //删除文件
                                storeStrategy.deleteFile(userDto.getUserId(), resource);
                            }
                        }
                        return true;
                    } catch (IOException e) {
                        log.error("文件保存失败", e);
                        return false;
                    }
                }
                //在根目录按服务端路径子目录结构创建
                case MIRROR_SOURCE_DIRECTORY: {
                    String filePath = file.getAbsolutePath();
                    String relativePath = filePath.substring(batchImportBo.getSourcePath().length());
                    //提取子目录
                    String catalogPath = getSubPath(relativePath);
                    if (isMore4Floors(catalogPath)) {
                        log.error("目录层级超过4层，无法导入{}", filePath);
                        return false;
                    }

                    Long cataId;
                    if (!"/".equals(catalogPath)) {
                        //不是根目录
                        cataId = userCatalogService.addCatalogPath(userDto, catalogPath);
                    } else {
                        cataId = rootCataId;
                    }
                    try (InputStream is = new FileInputStream(file)) {
                        saveFile(is,
                                file.getName(),
                                md5,
                                userDto,
                                batchImportBo.getCategoryId(),
                                cataId,
                                true,
                                false,
                                user.getStrategy());

                        for (UserResource resource : resources) {
                            resourceRepository.removeById(resource);
                            //删除文件
                            storeStrategy.deleteFile(userDto.getUserId(), resource);
                        }
                        return true;

                    } catch (IOException e) {
                        log.error("文件保存失败", e);
                        return false;
                    }
                }
                //不自动创建，保存到根目录
                case NO_AUTO_CREATE: {
                    try (InputStream is = new FileInputStream(file)) {
                        saveFile(is,
                                file.getName(),
                                md5,
                                userDto,
                                batchImportBo.getCategoryId(),
                                rootCataId,
                                true,
                                false,
                                user.getStrategy());

                        //一起存在到根目录只会有一份文件，只删除资源记录即可
                        for (UserResource resource : resources) {
                            resourceRepository.removeById(resource);
                        }
                        return true;
                    } catch (IOException e) {
                        log.error("文件保存失败", e);
                        return false;
                    }
                }
                default:
                    throw new CollectionException(ErrorCodeEnum.AUTO_CREATE_RULE_ERROR);
            }
        }
        else{
            try (InputStream is = new FileInputStream(file)) {
                saveFile(is,
                        file.getName(),
                        md5,
                        userDto,
                        batchImportBo.getCategoryId(),
                        batchImportBo.getCatalogId(),
                        true,
                        false,
                        user.getStrategy());

                //保存成功。删除原有记录
                if (!resources.isEmpty()) {
                    for (UserResource resource : resources) {
                        resourceRepository.removeById(resource);
                        //删除文件
                        storeStrategy.deleteFile(userDto.getUserId(), resource);
                    }
                }
                return true;
            } catch (IOException e) {
                log.error("文件保存失败", e);
                return false;
            }
        }
    }

    private String getSubPath(String filePath){
        String catalogPath = filePath.substring(0, filePath.lastIndexOf(File.separatorChar));
        if (File.separatorChar == '/') { // Linux系统
            catalogPath = catalogPath.replaceAll("//", "/");
        }
        else{
            catalogPath = catalogPath.replaceAll("\\\\", "/");
        }
        return catalogPath.replaceAll("//", "/");
    }

    /**
     * 目录层级超过4层，无法导入
     * @param catalogPath
     * @return
     */
    private boolean isMore4Floors(String catalogPath){
        if(catalogPath.split("/").length > 3){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * 获取可导入的文件列表
     * @param path
     * @param includeSubdirectories
     * @return
     */
    public List<File> listImportableFiles(String path, boolean includeSubdirectories) {
        List<File> result = new ArrayList<>();
        try {
            Path startPath = Paths.get(path);

            // 使用Java NIO的Files.walk递归遍历目录
            try(Stream<Path> pathStream = includeSubdirectories ?
                    Files.walk(startPath) :  // 包含子目录
                    Files.list(startPath))   // 仅当前目录
            {
                result = pathStream
                        .filter(Files::isRegularFile)  // 只处理常规文件
                        .map(Path::toFile)
                        .filter(this::isImportableFile)  // 过滤可导入的文件
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("扫描路径失败: {}", path, e);
        }
        return result;
    }

    /**
     * 判断文件是否可导入
     */
    private boolean isImportableFile(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return false; // 没有扩展名
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        try {
            FileTypeEnum fileType = FileTypeEnum.valueOf(extension);
            return fileType != FileTypeEnum.unknown; // 排除未知类型
        } catch (IllegalArgumentException e) {
            return false; // 排除不在枚举中的类型
        }
    }
}
