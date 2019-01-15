package com.example.demo.amrtomp3;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * Create By yxl on 2018/6/5
 */
public class AmrToMP3Utils {

    //https://www.cnblogs.com/Amaris-Lin/p/9186888.html   单独一个util
    private static Logger logger =Logger.getLogger(AmrToMP3Utils.class);

    /**
     * 将amr文件输入转为mp3格式
     * @param file
     * @return
     */
    public static InputStream amrToMP3(MultipartFile file) {
        String ffmpegPath = getLinuxOrWindowsFfmpegPath();
        Runtime runtime = Runtime.getRuntime();
        try {
            String filePath = copyFile(file.getInputStream(), file.getOriginalFilename());

            String substring = filePath.substring(0, filePath.lastIndexOf("."));

            String mp3FilePath = substring + ".mp3";

            //执行ffmpeg文件，将amr格式转为mp3
            //filePath ----> amr文件在临时文件夹中的地址
            //mp3FilePath  ----> 转换后的mp3文件地址
            Process p = runtime.exec(ffmpegPath + "ffmpeg -i " + filePath + " " + mp3FilePath);//执行ffmpeg.exe,前面是ffmpeg.exe的地址，中间是需要转换的文件地址，后面是转换后的文件地址。-i是转换方式，意思是可编码解码，mp3编码方式采用的是libmp3lame

            //释放进程
            p.getOutputStream().close();
            p.getInputStream().close();
            p.getErrorStream().close();
            p.waitFor();

            File mp3File = new File(mp3FilePath);
            InputStream fileInputStream = new FileInputStream(mp3File);

            //应该在调用该方法的地方关闭该input流（使用完后），并且要删除掉临时文件夹下的相应文件
            /*File amrFile = new File(filePath);
            File mp3File = new File(mp3FilePath);
            if (amrFile.exists()) {
                boolean delete = amrFile.delete();
                System.out.println("删除源文件："+delete);
            }
            if (mp3File.exists()) {
                boolean delete = mp3File.delete();
                System.out.println("删除mp3文件："+delete);
            }*/

            return fileInputStream;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runtime.freeMemory();
        }
        return null;
    }

    /**
     * 将amr文件输入流转为mp3格式
     * @param inputStream  amr文件的输入流（也可以是其它的文件流）
     * @param fileName  文件名（包含后缀）
     * @return
     */
    public static InputStream amrToMP3(InputStream inputStream, String fileName) {
        String ffmpegPath = getLinuxOrWindowsFfmpegPath();
        Runtime runtime = Runtime.getRuntime();
        try {
            String filePath = copyFile(inputStream, fileName);
            String substring = filePath.substring(0, filePath.lastIndexOf("."));
            String mp3FilePath = substring + ".mp3";

            //执行ffmpeg文件，将amr格式转为mp3
            //filePath ----> amr文件在临时文件夹中的地址
            //mp3FilePath  ----> 转换后的mp3文件地址
            Process p = runtime.exec(ffmpegPath + "ffmpeg -i" + " " +filePath + " " + mp3FilePath);//执行ffmpeg.exe,前面是ffmpeg.exe的地址，中间是需要转换的文件地址，后面是转换后的文件地址。-i是转换方式，意思是可编码解码，mp3编码方式采用的是libmp3lame

            //释放进程
            p.getOutputStream().close();
            p.getInputStream().close();
            p.getErrorStream().close();
            p.waitFor();

            File file = new File(mp3FilePath);
            InputStream fileInputStream = new FileInputStream(file);

            //应该在调用该方法的地方关闭该input流（使用完后），并且要删除掉临时文件夹下的相应文件
            /*File amrFile = new File(filePath);
            File mp3File = new File(mp3FilePath);
            if (amrFile.exists()) {
                boolean delete = amrFile.delete();
                System.out.println("删除源文件："+delete);
            }
            if (mp3File.exists()) {
                boolean delete = mp3File.delete();
                System.out.println("删除mp3文件："+delete);
            }*/
            return fileInputStream;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runtime.freeMemory();
        }
        return null;
    }

    /**
     * 将用户输入的amr音频文件流转为音频文件并存入临时文件夹中
     * @param inputStream  输入流
     * @param fileName  文件姓名
     * @return  amr临时文件存放地址
     * @throws IOException
     */
    private static String copyFile(InputStream inputStream, String fileName) throws IOException {
        Properties props = System.getProperties();
        String filePath = props.getProperty("user.home") + File.separator + "MP3TempFile"; //创建临时目录
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdir();
        }

        String outPutFile = dir + File.separator + fileName;

        OutputStream outputStream = new FileOutputStream(outPutFile);
        int bytesRead;
        byte[] buffer = new byte[8192];
        while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return outPutFile;
    }

    /**
     * 判断系统是Windows还是linux并且拼接ffmpegPath
     * @return
     */
    private static String getLinuxOrWindowsFfmpegPath() {
        String ffmpegPath = "";
        String osName = System.getProperties().getProperty("os.name");
        if (osName.toLowerCase().indexOf("linux") >= 0) {
            ffmpegPath = "";
        } else {
            URL url = Thread.currentThread().getContextClassLoader().getResource("ffmpeg/windows/");
            if (url != null) {
                ffmpegPath = url.getFile();
            }
        }
        return ffmpegPath;
    }
}
