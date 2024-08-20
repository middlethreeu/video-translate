package cn.iocoder.yudao.module.ai.controller.app.videotranslate;

import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslatePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslateRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo.AppVideoTranslateSaveReqVO;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import cn.iocoder.yudao.module.ai.service.videotranslate.VideoTranslateService;

@Tag(name = "用户 APP - 视频语音翻译字幕生成")
@RestController
@RequestMapping("/ai/video-translate")
@Validated
public class AppVideoTranslateController {

    @Resource
    private VideoTranslateService videoTranslateService;

    @PostMapping("/create")
    @Operation(summary = "创建视频语音翻译字幕生成")
    public CommonResult<Long> createVideoTranslate(@Valid @RequestBody AppVideoTranslateSaveReqVO createReqVO) {
        return success(videoTranslateService.createVideoTranslate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新视频语音翻译字幕生成")
    public CommonResult<Boolean> updateVideoTranslate(@Valid @RequestBody AppVideoTranslateSaveReqVO updateReqVO) {
        videoTranslateService.updateVideoTranslate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除视频语音翻译字幕生成")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteVideoTranslate(@RequestParam("id") Long id) {
        videoTranslateService.deleteVideoTranslate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得视频语音翻译字幕生成")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppVideoTranslateRespVO> getVideoTranslate(@RequestParam("id") Long id) {
        VideoTranslateDO videoTranslate = videoTranslateService.getVideoTranslate(id);
        return success(BeanUtils.toBean(videoTranslate, AppVideoTranslateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得视频语音翻译字幕生成分页")
    public CommonResult<PageResult<AppVideoTranslateRespVO>> getVideoTranslatePage(@Valid AppVideoTranslatePageReqVO pageReqVO) {
        PageResult<VideoTranslateDO> pageResult = videoTranslateService.getVideoTranslatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppVideoTranslateRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出视频语音翻译字幕生成 Excel")
    @ApiAccessLog(operateType = EXPORT)
    public void exportVideoTranslateExcel(@Valid AppVideoTranslatePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<VideoTranslateDO> list = videoTranslateService.getVideoTranslatePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "视频语音翻译字幕生成.xls", "数据", AppVideoTranslateRespVO.class,
                        BeanUtils.toBean(list, AppVideoTranslateRespVO.class));
    }

    @PostMapping("/translate")
    @Operation(summary = "翻译处理视频语音")
    public CommonResult<Boolean> translateVideo(@Valid @RequestParam("id") Long id,@RequestParam("srcLang") String srcLang,@RequestParam("targetLang1") String targetLang1,@RequestParam("targetLang2") String targetLang2) {
        videoTranslateService.translateVideo(id,srcLang,targetLang1,targetLang2);
        return success(true);
    }

}