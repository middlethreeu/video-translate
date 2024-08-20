package cn.iocoder.yudao.module.ai.dal.mysql.videotranslate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslatePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频语音翻译字幕生成 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VideoTranslateMapper extends BaseMapperX<VideoTranslateDO> {

    default PageResult<VideoTranslateDO> selectPage(AppVideoTranslatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VideoTranslateDO>()
                .betweenIfPresent(VideoTranslateDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(VideoTranslateDO::getVideoSrcName, reqVO.getVideoSrcName())
                .eqIfPresent(VideoTranslateDO::getState, reqVO.getState())
                .orderByDesc(VideoTranslateDO::getId));
    }

}