package cn.iocoder.yudao.module.ai.service.videotranslate;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslatePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslateSaveReqVO;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.ai.dal.mysql.videotranslate.VideoTranslateMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * 视频语音翻译字幕生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class VideoTranslateServiceImpl implements VideoTranslateService {

    @Resource
    private VideoTranslateMapper videoTranslateMapper;
    @Resource
    private FileApi fileApi;

    @Autowired
    private VideoHandleService videoHandleService;

    @Override
    public Long createVideoTranslate(AppVideoTranslateSaveReqVO createReqVO) {
        // 插入
        VideoTranslateDO videoTranslate = BeanUtils.toBean(createReqVO, VideoTranslateDO.class);
        videoTranslateMapper.insert(videoTranslate);
        // 返回
        return videoTranslate.getId();
    }

    @Override
    public void updateVideoTranslate(AppVideoTranslateSaveReqVO updateReqVO) {
        // 校验存在
        validateVideoTranslateExists(updateReqVO.getId());
        // 更新
        VideoTranslateDO updateObj = BeanUtils.toBean(updateReqVO, VideoTranslateDO.class);
        videoTranslateMapper.updateById(updateObj);
    }

    @Override
    public void deleteVideoTranslate(Long id) {
        // 校验存在
        validateVideoTranslateExists(id);
        // 删除
        videoTranslateMapper.deleteById(id);
    }

    private void validateVideoTranslateExists(Long id) {
        if (videoTranslateMapper.selectById(id) == null) {
            throw exception(VIDEO_TRANSLATE_NOT_EXISTS);
        }
    }

    @Override
    public VideoTranslateDO getVideoTranslate(Long id) {
        return videoTranslateMapper.selectById(id);
    }

    @Override
    public PageResult<VideoTranslateDO> getVideoTranslatePage(AppVideoTranslatePageReqVO pageReqVO) {
        return videoTranslateMapper.selectPage(pageReqVO);
    }

    @Override
    public VideoTranslateDO translateVideo(Long id,String srcLang,String targetLang1,String targetLang2){
        VideoTranslateDO videoTranslateDO = videoTranslateMapper.selectById(id);
        if (null != videoTranslateDO.getVideoSrcName() && !videoTranslateDO.getVideoSrcName().isEmpty()) {
            String localBasePath = fileApi.getBasePath();
            if (!localBasePath.endsWith("/") && !localBasePath.endsWith("\\")){
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    localBasePath = localBasePath +"\\";
                } else {
                    localBasePath = localBasePath +"/";
                }
            }
            String fileName = videoTranslateDO.getVideoSrcName().substring(StrUtil.lastIndexOfIgnoreCase(videoTranslateDO.getVideoSrcName(),"/")+1);
            videoHandleService.videoTranslate(videoTranslateDO, localBasePath, fileName, srcLang, targetLang1, targetLang2);
        }
        return videoTranslateDO;
    }

    @Async
    protected void sycVideoHandler(VideoTranslateDO videoTranslateDO ,String localBasePath,String fileName){
        String fileRealPath = localBasePath + fileName;
        String fileMp3 = localBasePath + fileName.substring(0,fileName.lastIndexOf(".")) + ".mp3";
        try {
            VideoCVService.videoToSpeech(fileRealPath, fileMp3);
            videoTranslateDO.setHandleInfo("语音提取成功！");
            videoTranslateDO.setHandleProcess(20);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);
        } catch (Exception e) {
            videoTranslateDO.setHandleInfo("语音提取失败：" + e.getMessage());
            videoTranslateDO.setState("error");
            videoTranslateMapper.updateById(videoTranslateDO);
        }
    }



}