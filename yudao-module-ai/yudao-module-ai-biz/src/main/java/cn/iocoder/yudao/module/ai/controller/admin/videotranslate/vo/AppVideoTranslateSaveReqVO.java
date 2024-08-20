package cn.iocoder.yudao.module.ai.controller.admin.videotranslate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "用户 APP - 视频语音翻译字幕生成新增/修改 Request VO")
@Data
public class AppVideoTranslateSaveReqVO {

    @Schema(description = "视频原生文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "视频原生文件名不能为空")
    private String videoSrcName;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.AUTO, example = "14200")
    private Long id;

}