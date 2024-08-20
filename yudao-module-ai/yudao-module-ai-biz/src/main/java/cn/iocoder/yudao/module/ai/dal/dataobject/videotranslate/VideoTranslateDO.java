package cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 视频语音翻译字幕生成 DO
 *
 * @author 芋道源码
 */
@TableName("ai_video_translate")
@KeySequence("ai_video_translate_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoTranslateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 视频随机文件名
     */
    private String videoGuidName;
    /**
     * 视频保存路径
     */
    private String videoPath;
    /**
     * 处理进度
     */
    private Integer handleProcess;
    /**
     * 处理信息
     */
    private String handleInfo;
    /**
     * 视频原生文件名
     */
    private String videoSrcName;
    /**
     * 状态
     *
     * 枚举 {@link TODO ai_video_type 对应的类}
     */
    private String state;

}