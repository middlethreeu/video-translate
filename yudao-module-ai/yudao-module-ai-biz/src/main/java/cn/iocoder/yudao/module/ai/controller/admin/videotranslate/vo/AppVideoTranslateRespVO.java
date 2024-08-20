package cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "用户 APP - 视频语音翻译字幕生成 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppVideoTranslateRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14200")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "处理进度")
    @ExcelProperty("处理进度")
    private Integer handleProcess;

    @Schema(description = "处理信息")
    @ExcelProperty("处理信息")
    private String handleInfo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "视频原生文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("视频原生文件名")
    private String videoSrcName;

    @Schema(description = "状态")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("ai_video_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String state;

}