package com.kochiu.collection.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.data.bo.BatchImportBo;
import com.kochiu.collection.data.bo.PathBo;
import com.kochiu.collection.data.bo.UploadBo;
import com.kochiu.collection.data.dto.ChunkUploadDto;
import com.kochiu.collection.data.dto.ResourceDto;
import com.kochiu.collection.data.dto.UserDto;
import com.kochiu.collection.data.vo.FileVo;
import com.kochiu.collection.entity.SysUser;
import com.kochiu.collection.entity.UserCatalog;
import com.kochiu.collection.entity.UserResource;
import com.kochiu.collection.enums.ErrorCodeEnum;
import com.kochiu.collection.enums.ImportMethodEnum;
import com.kochiu.collection.enums.ResourceTypeEnum;
import com.kochiu.collection.enums.SaveTypeEnum;
import com.kochiu.collection.exception.CollectionException;
import com.kochiu.collection.handler.ImportProgressWebSocketHandler;
import com.kochiu.collection.repository.SysUserRepository;
import com.kochiu.collection.repository.UserCatalogRepository;
import com.kochiu.collection.repository.UserCategoryRepository;
import com.kochiu.collection.repository.UserResourceRepository;
import com.kochiu.collection.service.file.FileStrategyFactory;
import com.kochiu.collection.service.store.ResourceStoreStrategy;
import com.kochiu.collection.service.store.ResourceStrategyFactory;
import com.kochiu.collection.service.store.ThumbnailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kochiu.collection.Constant.RANDOM_CHARS2;
import static com.kochiu.collection.Constant.ROOT_PATH;
import static com.kochiu.collection.enums.ErrorCodeEnum.FILE_IS_EXIST;
import static com.kochiu.collection.enums.ErrorCodeEnum.FILE_SAVING_FAILURE;
import static com.kochiu.collection.util.SysUtil.tidyPath;

@Slf4j
@Service
public class ResourceFileService {

    @Value("${file.upload.dir:}")
    private String uploadDir;

    private final ResourceStrategyFactory resourceStrategyFactory;
    private final SysUserRepository userRepository;
    private final UserResourceRepository resourceRepository;
    private final UserCatalogService userCatalogService;
    private final UserCatalogRepository catalogRepository;
    private final SystemService systemService;
    private final ObjectMapper objectMapper;
    private final UserCategoryRepository  userCategoryRepository;
    private final FileStrategyFactory fileStrategyFactory;
    private final ThumbnailService thumbnailService;
    private final UserResourceService userResourceService;

    public ResourceFileService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               UserCatalogService userCatalogService,
                               UserCatalogRepository catalogRepository,
                               SystemService systemService, ObjectMapper objectMapper,
                               UserCategoryRepository userCategoryRepository,
                               FileStrategyFactory fileStrategyFactory,
                               ThumbnailService thumbnailService,
                               UserResourceService userResourceService) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.userCatalogService = userCatalogService;
        this.catalogRepository = catalogRepository;
        this.systemService = systemService;
        this.objectMapper = objectMapper;
        this.userCategoryRepository = userCategoryRepository;
        this.fileStrategyFactory = fileStrategyFactory;
        this.thumbnailService = thumbnailService;
        this.userResourceService = userResourceService;
    }

    /**
     * 通过策略保存文件
     */
    @Transactional(rollbackFor = Exception.class)
    public FileVo saveFile(UploadBo uploadBo, UserDto userDto) throws CollectionException {

        return saveFile(uploadBo.getFile().getResource(),
                uploadBo.getCategoryId(),
                uploadBo.getCataId(),
                uploadBo.getOverwrite(),
                uploadBo.getAutoCreate(),
                uploadBo.getFile().getOriginalFilename(),
                userDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public FileVo saveFile(Resource file,
                           Long categoryId,
                           Long cataId,
                           Boolean overwrite,
                           Boolean autoCreate,
                           String originalFilename,
                           UserDto userDto) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("upload-", ".tmp");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            // 计算MD5
            String md5 = DigestUtil.md5Hex(tempFile.toFile());

            // 使用临时文件
            try (InputStream is = Files.newInputStream(tempFile)) {
                return saveFile(is,
                        originalFilename,
                        md5,
                        user,
                        categoryId,
                        cataId,
                        overwrite,
                        autoCreate);
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

    private void createThumbnail(FileVo fileVo, String strategy){
        //异步创建缩略图
        ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(strategy);
        UserResource resource = resourceRepository.getById(fileVo.getResourceId());
        if(resource == null){
            return;
        }
        ResourceDto resourceDto = ResourceDto.builder()
                .resourceId(fileVo.getResourceId())
                .resourceUrl(resource.getResourceUrl())
                .fileExt(resource.getFileExt())
                .filePath(resource.getFilePath())
                .build();

        FileType fileType = fileStrategyFactory.getFileType(resource.getFileExt());

        String filePath;
        if(resource.getSaveType() == SaveTypeEnum.LINK.getCode()){
            filePath = resource.getFilePath();
        }
        else {
            String recFilePathDir = storeStrategy.getServerUrl();
            filePath = tidyPath(recFilePathDir + resource.getFilePath());
        }

        thumbnailService.asyncCreateThumbnail(resourceDto, fileType, filePath, null, null);
    }

    //  检查文件是否存在
    public boolean checkFileExist(UserDto userDto, String md5) throws CollectionException {
        List<UserResource> resources = resourceRepository.countFileMd5(userDto.getUserId(), md5);
        return !resources.isEmpty();
    }

    /**
     * 保存文件
     */
    protected FileVo saveFile(InputStream inputStream,
                              String originalFilename,
                              String md5,
                              SysUser user,
                              Long categoryId,
                              Long cataId,
                              Boolean overwrite,
                              Boolean autoCreate
                              ) throws CollectionException {

        if(overwrite == null){
            overwrite = true;
        }

        List<UserResource> resources = resourceRepository.countFileMd5(user.getUserId(), md5);
        if(!resources.isEmpty() && !overwrite){
            throw new CollectionException(FILE_IS_EXIST);
        }

        if(categoryId != null) {
            if (userCategoryRepository.getById(categoryId) == null) {
                //取默认分类
                categoryId = userCategoryRepository.getDefaultCategory(user.getUserId());
                if(categoryId == null){
                    throw new CollectionException(ErrorCodeEnum.CATEGORY_NOT_EXIST);
                }
            }
        }
        if(autoCreate == null){
            autoCreate = cataId == null;
        }

        String savePath;
        if(autoCreate) {
            String catalogPath = ResourceStoreStrategy.dateFormat.format(System.currentTimeMillis());
            savePath = "/" + user.getUserCode() + "/" + catalogPath;
            //创建目录
            Long id = userCatalogService.addCatalogPath(user, catalogPath);
            if(id == null){
                throw new CollectionException(ErrorCodeEnum.CATALOG_CREATE_FAILURE);
            }
            else{
                cataId = id;
            }
        }
        else{
            if(cataId == null){
                throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
            }
            else{
                //非自动创建目录，检查目录是否存在
                UserCatalog catalog = catalogRepository.getById(cataId);
                if(catalog == null){
                    throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
                }
                savePath = "/" + user.getUserCode() + "/" + catalog.getCataPath();
            }
        }
        savePath = tidyPath(savePath);
        if(savePath.endsWith("/")){
            savePath = savePath.substring(0, savePath.length() - 1);
        }

        ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
        FileVo fileVo = storeStrategy.saveFile(inputStream,
                        originalFilename,
                        user, md5, savePath, categoryId, cataId);

        //保存成功。删除原有记录
        if(!resources.isEmpty() && overwrite){
            for(UserResource resource : resources){
                resourceRepository.removeById(resource);
                //如果路径和原来一样不删文件
                if(!resource.getResourceUrl().equals(fileVo.getResourceUrl())){
                    //删除文件
                    storeStrategy.deleteFile(user.getUserId(), resource);
                }
            }
        }
        //不返回给客户端
        fileVo.setResourceUrl(null);
        //转换缩略图路径
        UserResource resource = resourceRepository.getById(fileVo.getResourceId());
        fileVo.setUrl(userResourceService.buildVirtualUrl(user, resource, fileVo.getUrl()));
        fileVo.setThumbnailUrl(userResourceService.buildVirtualUrl(user, resource, fileVo.getThumbnailUrl()));
        return fileVo;
    }

    /**
     * 通过策略下载文件
     * @return
     */
    public ResponseEntity<Resource> downloadResource(HttpServletRequest request, HttpServletResponse response, List<HttpRange> ranges, Long resourceId) {

        //查找资源
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            return ResponseEntity.notFound().build();
        }

        int userId = resource.getUserId();
        SysUser user = userRepository.getById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return resourceStrategyFactory.getStrategy(user.getStrategy())
                .downloadResource(request, response, ranges, resourceId);
    }

    // 批量导入前检查
    public void beforeImport(UserDto userDto, BatchImportBo batchImportBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if (!systemService.testServerPath(PathBo.builder()
                .path(batchImportBo.getSourcePath())
                .importMethod(batchImportBo.getImportMethod())
                .build())) {
            throw new CollectionException(ErrorCodeEnum.SERVER_PATH_ERROR);
        }

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

        ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
        if(!storeStrategy.checkServerUrl()){
            throw new CollectionException(ErrorCodeEnum.SERVER_PATH_ERROR);
        }
    }


    /**
     * 批量导入
     */
    public void batchImport(String taskId, UserDto userDto, BatchImportBo batchImportBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        // 取出服务器路径里适合导入的文件
        List<File> files = listImportableFiles(batchImportBo.getSourcePath(),
                batchImportBo.isIncludeSubDir(), batchImportBo.getExcludePattern(),
                batchImportBo.getExcludeFileExt(), batchImportBo.getExcludeFileSize());
        // 取用户根目录ID
        Long rootCataId = catalogRepository.getUserRoot(user.getUserId());

        int successCount = 0, failCount = 0;
        try {
            ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
            for (int i = 0; i < files.size(); i++) {
                // 检查是否被取消（通过线程中断）
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("任务被取消");
                }

                File file = files.get(i);
                log.info("正在处理文件：{}", file.getName());
                String filePath = file.getAbsolutePath();
                String relativePath = filePath.substring(batchImportBo.getSourcePath().length());

                ImportProgressWebSocketHandler.ImportProgress progress = new ImportProgressWebSocketHandler.ImportProgress(i + 1, files.size(), successCount, failCount, filePath, "processing", null);
                ImportProgressWebSocketHandler.sendProgress(taskId, progress);

                // 获取文件MD5，查出相同文件的记录
                String md5 = DigestUtil.md5Hex(file);
                List<UserResource> resources = resourceRepository.countFileMd5(userDto.getUserId(), md5);

                switch (batchImportBo.getImportMethod()){
                    case COPY:
                    case MOVE: {
                        if(handleCopyOrMove(batchImportBo, file, md5, user, rootCataId, resources, storeStrategy)){
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
                    case KEEP_ORIGINAL_AND_CREATE_CATA:
                    case KEEP_ORIGINAL: {
                        try {
                            if(keepOriginal(user, batchImportBo, file, relativePath, storeStrategy, md5, rootCataId, resources)) {
                                successCount++;
                            }
                            else{
                                failCount++;
                            }
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
                progress = new ImportProgressWebSocketHandler.ImportProgress(i + 1, files.size(), successCount, failCount, filePath, "processing", null);
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
            throw new CollectionException(ErrorCodeEnum.IMPORT_ERROR);
        }
    }

    /**
     * 处理复制或移动
     */
    private boolean handleCopyOrMove(BatchImportBo batchImportBo, File file,
                                  String md5, SysUser user,
                                  Long rootCataId, List<UserResource> resources,
                                  ResourceStoreStrategy storeStrategy) throws CollectionException {

        //假如没有选择导入目录，下面的目录规则生效
        if(batchImportBo.getCatalogId() == null) {
            switch (batchImportBo.getAutoCreateRule()) {
                //在根目录创建日期目录
                case CREATE_DATE_DIRECTORY: {
                    try (InputStream is = new FileInputStream(file)) {
                        FileVo fileVo = saveFile(is,
                                file.getName(),
                                md5,
                                user,
                                batchImportBo.getCategoryId(),
                                rootCataId,
                                true,
                                true);
                        createThumbnail(fileVo, user.getStrategy());

                        //保存成功。删除原有记录
                        if (!resources.isEmpty()) {
                            for (UserResource resource : resources) {
                                resourceRepository.removeById(resource);
                                //删除文件
                                storeStrategy.deleteFile(user.getUserId(), resource);
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
                    if (isMore3Floors(catalogPath)) {
                        //目录层级超过3层，不再创建子目录，放到上一层
                        //取上一层目录
                        catalogPath = getParentPath(catalogPath);
                    }

                    Long cataId;
                    if (!"/".equals(catalogPath)) {
                        //不是根目录
                        cataId = userCatalogService.addCatalogPath(user, catalogPath);
                    } else {
                        cataId = rootCataId;
                    }
                    try (InputStream is = new FileInputStream(file)) {
                        FileVo fileVo = saveFile(is,
                                file.getName(),
                                md5,
                                user,
                                batchImportBo.getCategoryId(),
                                cataId,
                                true,
                                false);
                        createThumbnail(fileVo, user.getStrategy());

                        for (UserResource resource : resources) {
                            resourceRepository.removeById(resource);
                            //删除文件
                            storeStrategy.deleteFile(user.getUserId(), resource);
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
                        FileVo fileVo = saveFile(is,
                                file.getName(),
                                md5,
                                user,
                                batchImportBo.getCategoryId(),
                                rootCataId,
                                true,
                                false);
                        createThumbnail(fileVo, user.getStrategy());

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
                FileVo fileVo = saveFile(is,
                        file.getName(),
                        md5,
                        user,
                        batchImportBo.getCategoryId(),
                        batchImportBo.getCatalogId(),
                        true,
                        false);
                createThumbnail(fileVo, user.getStrategy());

                //保存成功。删除原有记录
                if (!resources.isEmpty()) {
                    for (UserResource resource : resources) {
                        resourceRepository.removeById(resource);
                        //删除文件
                        storeStrategy.deleteFile(user.getUserId(), resource);
                    }
                }
                return true;
            } catch (IOException e) {
                log.error("文件保存失败", e);
                return false;
            }
        }
    }

    private boolean keepOriginal(SysUser user, BatchImportBo batchImportBo,
                              File file, String relativePath,
                              ResourceStoreStrategy storeStrategy, String md5,
                              Long rootCataId, List<UserResource> resources) throws CollectionException{

        if(!resources.isEmpty()){
            log.error("文件保存失败，请勿重复保存");
            return false;
        }

        //虚拟目录
        Long cataId = rootCataId;
        // 提取子目录
        String catalogPath = getSubPath(relativePath);
        if(batchImportBo.getImportMethod() == ImportMethodEnum.KEEP_ORIGINAL_AND_CREATE_CATA) {

            cataId = batchImportBo.getCatalogId() == null ? rootCataId : batchImportBo.getCatalogId();

            //取选择的目录
            UserCatalog parentCatalog = userCatalogService.getParentCatalog(user, batchImportBo.getCatalogId());
            catalogPath = tidyPath(parentCatalog.getCataPath() + "/" + catalogPath);
            if (isMore3Floors(catalogPath)) {
                //目录层级超过3层，不再创建子目录，放到上一层
                //取上一层目录
                catalogPath = getParentPath(catalogPath);
            }
            if(!ROOT_PATH.equals(catalogPath) && catalogPath.endsWith("/")){
                catalogPath = catalogPath.substring(0, catalogPath.length() - 1);
            }
            catalogPath = tidyPath(catalogPath);

            if(!catalogPath.equals(parentCatalog.getCataPath())){
                log.debug("catalogPath: {}, parentPath: {}", catalogPath, parentCatalog.getCataPath());
                String path = catalogPath.substring(parentCatalog.getCataPath().length());
                cataId = userCatalogService.addCatalogPath(user,
                        batchImportBo.getCatalogId() == null ? rootCataId : batchImportBo.getCatalogId(),
                        path);
            }
        }
        // 调用 saveLinkResource 方法保存链接资源
        storeStrategy.saveLinkResource(file,
                user,
                md5,
                catalogPath, // 根据原目录结构保存缩略图
                batchImportBo.getCategoryId(),
                cataId
        );
        return true;
    }

    private String getParentPath(String catalogPath) {
        if (catalogPath == null || "/".equals(catalogPath)) {
            return "/";
        }
        if(catalogPath.startsWith("/")){
            catalogPath = catalogPath.substring(1);
        }
        if(catalogPath.endsWith("/")){
            catalogPath = catalogPath.substring(0, catalogPath.length() - 1);
        }
        
        // Remove the last directory name to get the parent path
        String[] pathParts = catalogPath.split("/");
        if (pathParts.length <= 1) {
            return "/";
        }
        
        StringBuilder parentPath = new StringBuilder();
        for (int i = 0; i < pathParts.length - 1; i++) {
            if (pathParts[i] != null && !pathParts[i].isEmpty()) {
                parentPath.append("/").append(pathParts[i]);
            }
        }
        
        String path = parentPath.toString().isEmpty() ? "/" : parentPath.toString();
        if (isMore3Floors(path)){
            return getParentPath(path);
        }
        return tidyPath(path);
    }

    private String getSubPath(String filePath){
        String catalogPath = filePath.substring(0, filePath.lastIndexOf(File.separatorChar));
        return tidyPath(catalogPath);
    }

    /**
     * 目录层级超过3层
     * @param catalogPath
     * @return
     */
    private boolean isMore3Floors(String catalogPath){
        catalogPath = tidyPath(catalogPath);
        if("/".equals(catalogPath)){
            return false;
        }
        if(catalogPath.startsWith("/")){
            catalogPath = catalogPath.substring(1);
        }
        if(catalogPath.endsWith("/")){
            catalogPath = catalogPath.substring(0, catalogPath.length() - 1);
        }
        return StringUtils.countMatches(catalogPath, "/") + 1 > 3;
    }

    /**
     * 获取可导入的文件列表（带过滤条件）
     */
    public List<File> listImportableFiles(String path, boolean includeSubdirectories,
                                          String excludePattern, String excludeFileExt,
                                          int excludeFileSize) {
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
                        .filter(file -> isImportableFile(file, excludePattern, excludeFileExt, excludeFileSize))  // 应用所有过滤条件
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("扫描路径失败: {}", path, e);
        }
        return result;
    }

    /**
     * 判断文件是否可导入（带过滤条件）
     */
    private boolean isImportableFile(File file, String excludePattern, String excludeFileExt, int excludeFileSize) {
        // 1. 检查文件大小
        if (excludeFileSize > 0 && file.length() > excludeFileSize * 1024L) {
            return false;
        }

        String fileName = file.getName();

        // 2. 检查排除模式（正则表达式）
        if (StringUtils.isNotBlank(excludePattern)) {
            try {
                Pattern pattern = Pattern.compile(excludePattern);
                if (pattern.matcher(fileName).matches()) {
                    return false;
                }
            } catch (PatternSyntaxException e) {
                log.warn("无效的正则表达式模式: {}", excludePattern);
            }
        }

        // 3. 检查文件扩展名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return false; // 没有扩展名
        }

        String extension = fileName.substring(dotIndex + 1).toLowerCase();

        // 检查排除的扩展名
        if (StringUtils.isNotBlank(excludeFileExt)) {
            List<String> excludedExts = Arrays.asList(excludeFileExt.toLowerCase().split(","));
            if (excludedExts.contains(extension)) {
                return false;
            }
        }

        // 4. 检查文件类型是否支持
        try {
            FileType fileType = fileStrategyFactory.getFileType(extension);
            return !fileType.desc().equals(ResourceTypeEnum.UNKNOWN); // 排除未知类型
        } catch (IllegalArgumentException e) {
            return false; // 排除不在枚举中的类型
        }
    }

    public void generateThumbnail(UserDto userDto, Long resourceId) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        FileVo fileVo = FileVo.builder()
                .resourceId(resourceId)
                .build();
        createThumbnail(fileVo, user.getStrategy());
    }

    // 重建缩略图
    public void rebuildThumbnail(UserDto userDto, Long resourceId) throws CollectionException {

        SysUser user = userRepository.getUser(userDto);
        //异步创建缩略图
        ResourceStoreStrategy storeStrategy = resourceStrategyFactory.getStrategy(user.getStrategy());
        UserResource resource = resourceRepository.getById(resourceId);
        if(resource == null){
            return;
        }
        ResourceDto resourceDto = ResourceDto.builder()
                .resourceId(resourceId)
                .resourceUrl(resource.getResourceUrl())
                .fileExt(resource.getFileExt())
                .filePath(resource.getFilePath())
                .build();

        FileType fileType = fileStrategyFactory.getFileType(resource.getFileExt());
        if(fileType.thumb()) {

            String filePath;
            String recFilePathDir = storeStrategy.getServerUrl();
            if(resource.getSaveType() == SaveTypeEnum.LINK.getCode()){
                filePath = resource.getFilePath();
            }
            else {
                filePath = tidyPath(recFilePathDir + resource.getFilePath());
            }
            String thumbFilePath = recFilePathDir + resource.getResourceUrl();
            thumbFilePath = thumbFilePath.replace("." + resourceDto.getFileExt(), "_thumb.png");
            String thumbUrl = resourceDto.getResourceUrl().replace("." + resourceDto.getFileExt(), "_thumb.png");

            thumbnailService.asyncCreateThumbnail(resourceDto, fileType, filePath, thumbFilePath, thumbUrl);
        }
    }

    // 存储分片文件
    public void storeChunk(ChunkUploadDto chunkDTO) throws IOException {

        //取系统临时文件夹
        uploadDir = StringUtils.isBlank(uploadDir) ? System.getProperty("java.io.tmpdir") : uploadDir;
        String chunkDir = uploadDir + File.separator + "chunks" + File.separator + chunkDTO.getFileId();
        File dir = new File(chunkDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String chunkFilename = chunkDTO.getChunkIndex() + ".part";
        Path filePath = Paths.get(chunkDir, chunkFilename);
        Files.write(filePath, chunkDTO.getFile().getBytes());
    }

    // 合并分片文件
    public File mergeChunks(String fileId) throws IOException {

        //取系统临时文件夹
        uploadDir = StringUtils.isBlank(uploadDir) ? System.getProperty("java.io.tmpdir") : uploadDir;
        String chunkDir = uploadDir + File.separator + "chunks" + File.separator + fileId;
        File[] chunks = new File(chunkDir).listFiles();

        if (chunks == null || chunks.length == 0) {
            throw new RuntimeException("No chunks found");
        }

        // 按分片序号排序
        Arrays.sort(chunks, Comparator.comparingInt(f -> Integer.parseInt(f.getName().split("\\.")[0])));

        // 创建最终文件
        String filePath = uploadDir + File.separator + RandomStringUtils.random(16, RANDOM_CHARS2) + ".tmp";
        try (RandomAccessFile destFile = new RandomAccessFile(filePath, "rw")) {
            byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲区
            for (File chunk : chunks) {
                try (FileInputStream fis = new FileInputStream(chunk)) {
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        destFile.write(buffer, 0, len);
                    }
                }
                chunk.delete(); // 合并后删除分片
            }
        }

        // 删除分片目录
        Files.delete(Paths.get(chunkDir));

        return new File(filePath);
    }

    // 检查分片是否已上传
    public boolean checkChunk(String fileId, Integer chunkIndex) {
        //取系统临时文件夹
        uploadDir = StringUtils.isBlank(uploadDir) ? System.getProperty("java.io.tmpdir") : uploadDir;
        String chunkPath = uploadDir + File.separator + "chunks" + File.separator + fileId + File.separator + chunkIndex + ".part";
        return new File(chunkPath).exists();
    }
}
