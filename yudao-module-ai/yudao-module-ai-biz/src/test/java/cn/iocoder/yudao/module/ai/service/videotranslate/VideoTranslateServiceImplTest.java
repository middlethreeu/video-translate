package cn.iocoder.yudao.module.ai.service.videotranslate;

import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslatePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslateSaveReqVO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import cn.iocoder.yudao.module.ai.dal.mysql.videotranslate.VideoTranslateMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link VideoTranslateServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(VideoTranslateServiceImpl.class)
public class VideoTranslateServiceImplTest extends BaseDbUnitTest {

    @Resource
    private VideoTranslateServiceImpl videoTranslateService;

    @Resource
    private VideoTranslateMapper videoTranslateMapper;

    @Test
    public void testCreateVideoTranslate_success() {
        // 准备参数
        AppVideoTranslateSaveReqVO createReqVO = randomPojo(AppVideoTranslateSaveReqVO.class).setId(null);

        // 调用
        Long videoTranslateId = videoTranslateService.createVideoTranslate(createReqVO);
        // 断言
        assertNotNull(videoTranslateId);
        // 校验记录的属性是否正确
        VideoTranslateDO videoTranslate = videoTranslateMapper.selectById(videoTranslateId);
        assertPojoEquals(createReqVO, videoTranslate, "id");
    }

    @Test
    public void testUpdateVideoTranslate_success() {
        // mock 数据
        VideoTranslateDO dbVideoTranslate = randomPojo(VideoTranslateDO.class);
        videoTranslateMapper.insert(dbVideoTranslate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AppVideoTranslateSaveReqVO updateReqVO = randomPojo(AppVideoTranslateSaveReqVO.class, o -> {
            o.setId(dbVideoTranslate.getId()); // 设置更新的 ID
        });

        // 调用
        videoTranslateService.updateVideoTranslate(updateReqVO);
        // 校验是否更新正确
        VideoTranslateDO videoTranslate = videoTranslateMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, videoTranslate);
    }

    @Test
    public void testUpdateVideoTranslate_notExists() {
        // 准备参数
        AppVideoTranslateSaveReqVO updateReqVO = randomPojo(AppVideoTranslateSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> videoTranslateService.updateVideoTranslate(updateReqVO), VIDEO_TRANSLATE_NOT_EXISTS);
    }

    @Test
    public void testDeleteVideoTranslate_success() {
        // mock 数据
        VideoTranslateDO dbVideoTranslate = randomPojo(VideoTranslateDO.class);
        videoTranslateMapper.insert(dbVideoTranslate);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbVideoTranslate.getId();

        // 调用
        videoTranslateService.deleteVideoTranslate(id);
       // 校验数据不存在了
       assertNull(videoTranslateMapper.selectById(id));
    }

    @Test
    public void testDeleteVideoTranslate_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> videoTranslateService.deleteVideoTranslate(id), VIDEO_TRANSLATE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetVideoTranslatePage() {
       // mock 数据
       VideoTranslateDO dbVideoTranslate = randomPojo(VideoTranslateDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setVideoSrcName(null);
           o.setState(null);
       });
       videoTranslateMapper.insert(dbVideoTranslate);
       // 测试 createTime 不匹配
       videoTranslateMapper.insert(cloneIgnoreId(dbVideoTranslate, o -> o.setCreateTime(null)));
       // 测试 videoSrcName 不匹配
       videoTranslateMapper.insert(cloneIgnoreId(dbVideoTranslate, o -> o.setVideoSrcName(null)));
       // 测试 state 不匹配
       videoTranslateMapper.insert(cloneIgnoreId(dbVideoTranslate, o -> o.setState(null)));
       // 准备参数
       AppVideoTranslatePageReqVO reqVO = new AppVideoTranslatePageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setVideoSrcName(null);
       reqVO.setState(null);

       // 调用
       PageResult<VideoTranslateDO> pageResult = videoTranslateService.getVideoTranslatePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbVideoTranslate, pageResult.getList().get(0));
    }

}