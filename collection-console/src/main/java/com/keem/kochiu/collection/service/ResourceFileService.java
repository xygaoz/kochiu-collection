package com.keem.kochiu.collection.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.keem.kochiu.collection.data.bo.BatchImportBo;
import com.keem.kochiu.collection.data.bo.PathBo;
import com.keem.kochiu.collection.data.bo.UploadBo;
import com.keem.kochiu.collection.data.dto.UserDto;
import com.keem.kochiu.collection.data.vo.FileVo;
import com.keem.kochiu.collection.entity.SysUser;
import com.keem.kochiu.collection.entity.UserCatalog;
import com.keem.kochiu.collection.entity.UserResource;
import com.keem.kochiu.collection.enums.ErrorCodeEnum;
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
import java.io.IOException;
import java.util.List;

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

    public ResourceFileService(ResourceStrategyFactory resourceStrategyFactory,
                               SysUserRepository userRepository,
                               UserResourceRepository resourceRepository,
                               UserCatalogService userCatalogService,
                               UserCatalogRepository catalogRepository,
                               SystemService systemService) {
        this.resourceStrategyFactory = resourceStrategyFactory;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.userCatalogService = userCatalogService;
        this.catalogRepository = catalogRepository;
        this.systemService = systemService;
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
            String md5 = DigestUtil.md5Hex(DigestUtil.md5Hex(uploadBo.getFile().getBytes()));
            List<UserResource> resources =resourceRepository.countFileMd5(user.getUserId(), md5);
            if(!resources.isEmpty() && !uploadBo.isOverwrite()){
                throw new CollectionException(FILE_IS_EXIST);
            }

            UserCatalog catalog = catalogRepository.getById(uploadBo.getCataId());
            if(catalog == null){
                throw new CollectionException(ErrorCodeEnum.CATEGORY_NOT_EXIST);
            }
            if(!uploadBo.isAutoCreate() && (uploadBo.getCataId() == null || catalogRepository.getById(uploadBo.getCataId()) == null)){
                //非自动创建目录，检查目录是否存在
                throw new CollectionException(ErrorCodeEnum.CATALOG_NOT_EXIST);
            }

            String path;
            if(uploadBo.isAutoCreate()) {
                String catalogPath = ResourceStoreStrategy.dateFormat.format(System.currentTimeMillis());
                path = "/" + user.getUserCode() + "/" + catalogPath;
                //创建目录
                Long id = userCatalogService.addCatalogPath(userDto, catalogPath);
                if(id == null){
                    throw new CollectionException(ErrorCodeEnum.CATALOG_CREATE_FAILURE);
                }
                else{
                    uploadBo.setCataId(id);
                }
            }
            else{
                path = "/" + user.getUserCode() + "/" + catalog.getCataPath();
            }
            path = path.replaceAll("//", "/");
            if(path.endsWith("/")){
                path = path.substring(0, path.length() - 1);
            }

            FileVo fileVo = resourceStrategyFactory.getStrategy(user.getStrategy())
                    .saveFile(uploadBo, userDto, md5, path);
            //保存成功。删除原有记录
            if(!resources.isEmpty() && uploadBo.isOverwrite()){
                resourceRepository.removeById(resources.get(0).getResourceId(), true);
            }
            return fileVo;
        }catch (IOException e){
            log.error("文件保存失败", e);
            throw new CollectionException(FILE_SAVING_FAILURE);
        }
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


    public void batchImport(String taskId, UserDto userDto, BatchImportBo batchImportBo) throws CollectionException {
        SysUser user = userRepository.getUser(userDto);
        if(!systemService.testServerPath(PathBo.builder().
                path(batchImportBo.getSourcePath()).
                importMethod(batchImportBo.getImportMethod())
                .build())){
            throw new CollectionException(ErrorCodeEnum.SERVER_PATH_ERROR);
        }

        try {
            for (int i = 1; i <= 100; i++) {
                Thread.sleep(100); // 模拟处理延迟
                ImportProgressWebSocketHandler.ImportProgress progress =
                        new ImportProgressWebSocketHandler.ImportProgress(
                                i, 100, "file_" + i + ".txt", "processing", null
                        );
                ImportProgressWebSocketHandler.sendProgress(taskId, progress);
            }
            // 任务完成
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(
                            100, 100, "", "completed", null
                    ));
        } catch (InterruptedException e) {
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(
                            0, 100, "", "error", "导入被中断"
                    ));
        } catch (Exception e) {
            ImportProgressWebSocketHandler.sendProgress(taskId,
                    new ImportProgressWebSocketHandler.ImportProgress(
                            0, 100, "", "error", e.getMessage()
                    ));
        }

    }
}
