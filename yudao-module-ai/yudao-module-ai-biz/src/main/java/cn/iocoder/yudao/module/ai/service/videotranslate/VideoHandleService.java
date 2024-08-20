package cn.iocoder.yudao.module.ai.service.videotranslate;

import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;

public interface VideoHandleService {
    public void videoTranslate(VideoTranslateDO videoTranslateDO , String localBasePath, String fileName,String srcLang,String targetLang1,String targetLang2);
}
