package cn.iocoder.yudao.module.infra.api.file;

import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 文件 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FileApiImpl implements FileApi {

    @Resource
    private FileService fileService;
    @Resource
    private FileConfigService fileconfigService;

    @Override
    public String createFile(String name, String path, byte[] content) {
        return fileService.createFile(name, path, content);
    }

    @Override
    public String getBasePath(){
        FileClientConfig fileClientConfig = fileconfigService.getDefultConfig();
        if(fileClientConfig instanceof LocalFileClientConfig){
            return
                ((LocalFileClientConfig) fileClientConfig).getBasePath();
        }else{
            return null;
        }
    }

}
