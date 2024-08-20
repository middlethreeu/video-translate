package cn.iocoder.yudao.module.ai.service.videotranslate;

import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.dal.dataobject.videotranslate.VideoTranslateDO;
import cn.iocoder.yudao.module.ai.dal.mysql.videotranslate.VideoTranslateMapper;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechModel;
import org.springframework.ai.openai.audio.transcription.AudioTranscription;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioTranscriptionResponseMetadata;
import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@Validated
public class VideoHandleServiceImpl implements VideoHandleService {

    @Resource
    private AiApiKeyService aiApiKeyService;
    @Resource
    private VideoTranslateMapper videoTranslateMapper;
    @Async
    public void videoTranslate(VideoTranslateDO videoTranslateDO , String localBasePath, String fileName,String srcLang,String targetLang1,String targetLang2) {
        String fileRealPath = localBasePath + fileName;
        String fileMp3 = localBasePath + fileName.substring(0,fileName.lastIndexOf(".")) + ".mp3";
        File mp3File = new File(fileMp3);
        if(mp3File.exists()){
            mp3File.delete();
        }
        String failMsg = "语音提取失败！";
        try {
            video2mp3(fileRealPath, fileMp3);
            videoTranslateDO.setHandleInfo("语音提取成功！");
            videoTranslateDO.setHandleProcess(20);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);

            failMsg = "语音转字幕失败！";
        String srcSrtStr = "";
            srcSrtStr = mp32srt(fileMp3);
            videoTranslateDO.setHandleInfo("语音转字幕成功！");
            videoTranslateDO.setHandleProcess(40);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);

            failMsg = "翻译字幕失败！";
            String translateSrt = "";
            if(srcSrtStr.equals(targetLang1)){ //判断是不是源语言，源语言不需要翻译
                translateSrt = srcSrtStr;
            }else {
                translateSrt = translateSrt(srcLang, srcSrtStr, targetLang1);
            }
            videoTranslateDO.setHandleInfo("翻译字幕成功！");
            videoTranslateDO.setHandleProcess(50);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);

            failMsg = "保存翻译后的字幕失败！";
                    String srtFile = localBasePath + fileName.substring(0,fileName.lastIndexOf(".")) + "_1.srt";
        writeSrtFile(translateSrt,srtFile);
            videoTranslateDO.setHandleInfo("字幕文件保存成功！");
            videoTranslateDO.setHandleProcess(60);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);
            String srtFile2 = "";
            if (targetLang2 != null && !"".equals(targetLang2)) {
                String translateSrt2 = "";
                if(srcSrtStr.equals(targetLang2)){ //判断是不是源语言，源语言不需要翻译
                    translateSrt2 = srcSrtStr;
                }else {
                    translateSrt2 = translateSrt(srcLang, srcSrtStr, targetLang2);
                }
                videoTranslateDO.setHandleInfo("翻译字幕2成功！");
                videoTranslateDO.setHandleProcess(70);
                videoTranslateDO.setState("process");
                videoTranslateMapper.updateById(videoTranslateDO);

                srtFile2 = localBasePath + fileName.substring(0,fileName.lastIndexOf(".")) + "_2.srt";
                writeSrtFile(translateSrt2,srtFile2);
                videoTranslateDO.setHandleInfo("字幕文件2保存成功！");
                videoTranslateDO.setHandleProcess(80);
                videoTranslateDO.setState("process");
                videoTranslateMapper.updateById(videoTranslateDO);
            }

            failMsg = "字幕植入视频失败！" ;
                    String translatedVidioFile = localBasePath + fileName.substring(0,fileName.lastIndexOf("."))+"_translated" + fileName.substring(fileName.lastIndexOf("."));
        srtInsertVidio(fileRealPath,srtFile,translatedVidioFile,localBasePath,srtFile2);
            videoTranslateDO.setHandleInfo("字幕植入视频成功！");
            videoTranslateDO.setHandleProcess(100);
            videoTranslateDO.setState("process");
            videoTranslateMapper.updateById(videoTranslateDO);

        } catch (Exception e) {
            log.error("translate video error !",e);
            videoTranslateDO.setHandleInfo("处理失败：" + failMsg );
            videoTranslateDO.setState("error");
            videoTranslateMapper.updateById(videoTranslateDO);
            return;
        }
    }

    private String translateSrt(String language,String srtStr,String targetLang){
        String[] srtLines = srtStr.split("\n\n");
        String translatedSrt = "";
        ChatModel chatModel = aiApiKeyService.getChatModel(AiPlatformEnum.OPENAI);
        Message message = new SystemMessage(String.format("You are a professional translator in %s and %s",language,targetLang));
        for(String srtLine : srtLines){
            if(srtLine.trim() == "")
                break;
            String[] srtLineArr = srtLine.split("\n");
            String srtLineArr0 = srtLineArr[0];
            String srtLineArr1 = srtLineArr[1];
            String srtLineArr2 = srtLineArr[2];
            Message message1 = new UserMessage("Reply directly to "+targetLang+" translation results. Note: Just give the translation results, prohibited to return anything other! Content to be translated:\n" + srtLineArr2);
            Prompt prompt = new Prompt(Lists.newArrayList(message, message1));
            ChatResponse chatResponse = ((OpenAiChatModel) chatModel).call(prompt);
            String stranText = (chatResponse.getResult().getOutput().getContent());

//            translatedSrt = translatedSrt + srtLineArr0+ "\n" + srtLineArr1 + "\n" + "<font color=#ffffff><font face=System><font size=60> " + stranText + "\n\n";
            translatedSrt = translatedSrt + srtLineArr0+ "\n" + srtLineArr1 + "\n" + stranText + "\n\n";
        }

        return translatedSrt;
    }


//    private String translateSrt(String language,String srtStr,String targetLang){
//        String[] srtLines = srtStr.split("\n\n");
//        String translatedSrt = "";
//        ArrayList textList = new ArrayList();
//        ChatModel chatModel = aiApiKeyService.getChatModel(AiPlatformEnum.OPENAI);
//        Message message = new SystemMessage(String.format("You are a professional translator in %s and %s",language,targetLang));
//        String willTranlsteText = "";
//        for(String srtLine : srtLines){
//            if(srtLine.trim() == "")
//                break;
//            String[] srtLineArr = srtLine.split("\n");
//            String srtLineArr0 = srtLineArr[0];
//            String srtLineArr1 = srtLineArr[1];
//            String srtLineArr2 = srtLineArr[2];
//            willTranlsteText = willTranlsteText +"" + srtLineArr2;
////            translatedSrt = translatedSrt + srtLineArr0+ "\n" + srtLineArr1 + "\n" + "<font color=#ffffff><font face=System><font size=60> " + stranText + "\n\n";
////            translatedSrt = translatedSrt + srtLineArr0+ "\n" + srtLineArr1 + "\n" + stranText + "\n\n";
//        }
//        Message message1 = new UserMessage("Reply directly to "+targetLang+" translation results. Note: Just give the translation results, prohibited to return anything other! Content to be translated:\n" + (willTranlsteText.isEmpty()?"":willTranlsteText));
//        Prompt prompt = new Prompt(Lists.newArrayList(message, message1));
//        ChatResponse chatResponse = ((OpenAiChatModel) chatModel).call(prompt);
//        String stranText = (chatResponse.getResult().getOutput().getContent());
//        String[] translatedArr = stranText.split("。");
//        int j = 0;
//        for(int i = 0; i < srtLines.length; i++) {
//            if (srtLines[i].trim() == "")
//                break;
//            String[] srtLineArr = srtLines[i].split("\n");
//            if(srtLineArr.length<3){
//                break;
//            }
//            String srtLineArr0 = srtLineArr[0];
//            String srtLineArr1 = srtLineArr[1];
//            String srtLineArr2 = srtLineArr[2];
//            translatedSrt = translatedSrt + srtLineArr0+ "\n" + srtLineArr1 + "\n" + srtLineArr2 + "\n" + translatedArr[j] + "\n\n";
//            if(srtLineArr2.endsWith(".") && translatedArr.length > (j + 1)){
//                j++;
//            }
//        }
//        return translatedSrt;
//    }

    private String mp32srt(String fileMp3){

        List<OpenAiAudioApi.StructuredResponse.Segment> segments = new ArrayList<>();
        String srtStr = "";
        OpenAiAudioApi openAiAudioApi = aiApiKeyService.getOpenAiAudioApi(AiPlatformEnum.OPENAI);
        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.SRT;

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
//                .withLanguage("en")
                .withModel("whisper-1")
                .withPrompt("Don’t make each line too long.")
                .withTemperature(0.8f)
                .withResponseFormat(responseFormat)
//                .withGranularityType(OpenAiAudioApi.TranscriptionRequest.GranularityType.SEGMENT)
                .build();
        var audioFile = new FileSystemResource(fileMp3);
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
//        AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(transcriptionRequest);
        Object response = RetryUtils.DEFAULT_RETRY_TEMPLATE.execute((ctx) -> {
            org.springframework.core.io.Resource audioResource = transcriptionRequest.getInstructions();
            OpenAiAudioApi.TranscriptionRequest requestBody = this.createRequestBody(transcriptionRequest);
            ResponseEntity transcriptionEntity;
            AudioTranscription transcript;
            RateLimit rateLimits;
            if (requestBody.responseFormat().isJsonType()) {
                transcriptionEntity = openAiAudioApi.createTranscription(requestBody, OpenAiAudioApi.StructuredResponse.class);
                OpenAiAudioApi.StructuredResponse transcriptionx = (OpenAiAudioApi.StructuredResponse) transcriptionEntity.getBody();
                return transcriptionx;
//                if (transcriptionx == null) {
//                    this.logger.warn("No transcription returned for request: {}", audioResource);
//                    return new AudioTranscriptionResponse((AudioTranscription)null);
//                } else {
//                    transcript = new AudioTranscription(transcriptionx.text());
//                    rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(transcriptionEntity);
//                    return new AudioTranscriptionResponse(transcript, OpenAiAudioTranscriptionResponseMetadata.from((OpenAiAudioApi.StructuredResponse)transcriptionEntity.getBody()).withRateLimit(rateLimits));
//                }
            } else {
                transcriptionEntity = openAiAudioApi.createTranscription(requestBody, String.class);
                String transcription = (String) transcriptionEntity.getBody();
                return transcription;
//                if (transcription == null) {
//                    this.logger.warn("No transcription returned for request: {}", audioResource);
//                    return new AudioTranscriptionResponse((AudioTranscription)null);
//                } else {
//                    transcript = new AudioTranscription(transcription);
//                    rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(transcriptionEntity);
//                    return new AudioTranscriptionResponse(transcript, OpenAiAudioTranscriptionResponseMetadata.from((String)transcriptionEntity.getBody()).withRateLimit(rateLimits));
//                }
            }
        });

        if(response instanceof OpenAiAudioApi.StructuredResponse){
            segments = ((OpenAiAudioApi.StructuredResponse) response).segments();
        } else if (response instanceof String){
            srtStr = (String) response;
        }
        return srtStr;
    }
    OpenAiAudioApi.TranscriptionRequest createRequestBody(AudioTranscriptionPrompt request) {
        OpenAiAudioTranscriptionOptions options = new OpenAiAudioTranscriptionOptions();
        if (request.getOptions() != null) {
            ModelOptions var4 = request.getOptions();
            if (!(var4 instanceof OpenAiAudioTranscriptionOptions)) {
                throw new IllegalArgumentException("Prompt options are not of type TranscriptionOptions: " + request.getOptions().getClass().getSimpleName());
            }

            OpenAiAudioTranscriptionOptions runtimeOptions = (OpenAiAudioTranscriptionOptions)var4;
            options = this.merge(runtimeOptions, options);
        }

        OpenAiAudioApi.TranscriptionRequest audioTranscriptionRequest = OpenAiAudioApi.TranscriptionRequest.builder().withFile(this.toBytes(request.getInstructions())).withResponseFormat(options.getResponseFormat()).withPrompt(options.getPrompt()).withTemperature(options.getTemperature()).withLanguage(options.getLanguage()).withModel(options.getModel()).withGranularityType(options.getGranularityType()).build();
        return audioTranscriptionRequest;
    }


    private byte[] toBytes(org.springframework.core.io.Resource resource) {
        try {
            return resource.getInputStream().readAllBytes();
        } catch (Exception var3) {
            throw new IllegalArgumentException("Failed to read resource: " + resource, var3);
        }
    }

    private OpenAiAudioTranscriptionOptions merge(OpenAiAudioTranscriptionOptions source, OpenAiAudioTranscriptionOptions target) {
        if (source == null) {
            source = new OpenAiAudioTranscriptionOptions();
        }

        OpenAiAudioTranscriptionOptions merged = new OpenAiAudioTranscriptionOptions();
        merged.setLanguage(source.getLanguage() != null ? source.getLanguage() : target.getLanguage());
        merged.setModel(source.getModel() != null ? source.getModel() : target.getModel());
        merged.setPrompt(source.getPrompt() != null ? source.getPrompt() : target.getPrompt());
        merged.setResponseFormat(source.getResponseFormat() != null ? source.getResponseFormat() : target.getResponseFormat());
        merged.setTemperature(source.getTemperature() != null ? source.getTemperature() : target.getTemperature());
        merged.setGranularityType(source.getGranularityType() != null ? source.getGranularityType() : target.getGranularityType());
        return merged;
    }
    private void video2mp3(String videoPath, String mp3Path) throws Exception {
        CommandLine cmdLine = new CommandLine("ffmpeg");
        cmdLine.addArgument("-loglevel");
        cmdLine.addArgument("error");
        cmdLine.addArgument("-i");
        cmdLine.addArgument(videoPath);
        cmdLine.addArgument("-vn");
        cmdLine.addArgument("-acodec");
        cmdLine.addArgument("libmp3lame");
        cmdLine.addArgument("-ab");
        cmdLine.addArgument("320k");
        cmdLine.addArgument("-f");
        cmdLine.addArgument("mp3");
        cmdLine.addArgument(mp3Path);

        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(3600000); // 1小时超时
        executor.setWatchdog(watchdog);
        executor.setExitValue(0);

        try {
            executor.execute(cmdLine);
        } catch (ExecuteException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    private void writeSrtFile(String srt,String srtFile){
        File file = new File(srtFile);
        if(file.exists()){
            file.delete();
        }
        try (FileWriter fileWriter = new FileWriter(file)){
            fileWriter.write(srt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void srtInsertVidio(String videoFile, String srtFile, String outFile, String localBasePath, String srtFile2) throws Exception {
        File file = new File(outFile);
        if(file.exists()){
            file.delete();
        }
        CommandLine cmdLine = new CommandLine("ffmpeg");
//        cmdLine.addArgument("-loglevel");
//        cmdLine.addArgument("error");
//        cmdLine.addArgument("-i");
//        cmdLine.addArgument(videoFile);
//        cmdLine.addArgument("-i");
//        cmdLine.addArgument(srtFile);
//        cmdLine.addArgument("-c:v");
//        cmdLine.addArgument("libx264");
//        cmdLine.addArgument("-crf");
//        cmdLine.addArgument("23");
//        cmdLine.addArgument("-y");
//        cmdLine.addArgument("-c:a");
//        cmdLine.addArgument("copy");
//        cmdLine.addArgument("-c:s");
//        cmdLine.addArgument("mov_text");
//        cmdLine.addArgument("-preset");
//        cmdLine.addArgument("medium");
//        cmdLine.addArgument(outFile);
        cmdLine.addArgument("-loglevel");
        cmdLine.addArgument("error");
        cmdLine.addArgument("-i");
        cmdLine.addArgument(videoFile);
        cmdLine.addArgument("-lavfi");
        String subtitles = "subtitles="+srtFile.replace(localBasePath,"")+":force_style='FontName=Arial,FontSize=22"+(srtFile2.isEmpty()?"":",MarginV=30")+",PrimaryColour=&HFFFFFF&,Outline=1,Shadow=0,BackColour=&H9C9C9C&,Bold=-1,Alignment=2'";
        if (!srtFile2.isEmpty()) {
            subtitles = subtitles + ",subtitles="+srtFile2.replace(localBasePath,"")+":force_style='FontName=Arial,FontSize=16,PrimaryColour=&HFFFFFF&,Outline=1,Shadow=0,BackColour=&H9C9C9C&,Bold=-1,Alignment=2'";
        }
        subtitles = subtitles +"";
        cmdLine.addArgument(subtitles,false);
        cmdLine.addArgument("-preset");
        cmdLine.addArgument("medium");
        cmdLine.addArgument("-c:v");
        cmdLine.addArgument("libx264");
        cmdLine.addArgument("-crf");
        cmdLine.addArgument("23");
        cmdLine.addArgument("-y");
        cmdLine.addArgument("-c:a");
        cmdLine.addArgument("copy");
        cmdLine.addArgument(outFile);

        DefaultExecutor executor = new DefaultExecutor();
        File workingDir = new File(localBasePath);
        executor.setWorkingDirectory(workingDir);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(3600000); // 1小时超时
        executor.setWatchdog(watchdog);
        executor.setExitValue(0);
        System.out.println(cmdLine.toString());
        try {
            executor.execute(cmdLine);
        } catch (ExecuteException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }


}
