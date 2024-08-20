package cn.iocoder.yudao.module.ai.service.videotranslate;

import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslatePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslateSaveReqVO;
import jakarta.validation.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 视频语音翻译字幕生成 Service 接口
 *
 * @author 芋道源码
 */
public interface VideoTranslateService {

    /**
     * 创建视频语音翻译字幕生成
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVideoTranslate(@Valid AppVideoTranslateSaveReqVO createReqVO);

    /**
     * 更新视频语音翻译字幕生成
     *
     * @param updateReqVO 更新信息
     */
    void updateVideoTranslate(@Valid AppVideoTranslateSaveReqVO updateReqVO);

    /**
     * 删除视频语音翻译字幕生成
     *
     * @param id 编号
     */
    void deleteVideoTranslate(Long id);

    /**
     * 获得视频语音翻译字幕生成
     *
     * @param id 编号
     * @return 视频语音翻译字幕生成
     */
    VideoTranslateDO getVideoTranslate(Long id);

    /**
     * 获得视频语音翻译字幕生成分页
     *
     * @param pageReqVO 分页查询
     * @return 视频语音翻译字幕生成分页
     */
    PageResult<VideoTranslateDO> getVideoTranslatePage(AppVideoTranslatePageReqVO pageReqVO);

    VideoTranslateDO translateVideo(Long id,String srcLang,String targetLang1,String targetLang2);
}