package cn.iocoder.yudao.module.ai.service.videotranslate;

import org.bytedeco.javacv.*;
public class VideoCVService {

    public static void videoToSpeech(String inputFile,String outputFile) throws Exception{
//        String inputFile = "input.mp4"; // 输入的视频文件路径
//        String outputFile = "output.mp3"; // 输出的 MP3 文件路径

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getAudioChannels())) {
            grabber.start();
            recorder.start();

            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                if (frame.streamIndex == grabber.getAudioStream()) {
                    recorder.record(frame);
                }
            }

            grabber.stop();
            recorder.stop();
        } catch (Exception e) {
            throw e;
        }
    }
}
