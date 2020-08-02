package cn.tyl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.tyl.entity.EntryBean;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;

import java.io.*;
import java.util.Date;


public class MergerBilibili {


    public static void main(String[] args) {

        String path = "C:\\Users\\tyl-7\\Desktop\\video";
        try {
            getAll(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 视频中获取音频文件
     */

    // FFmpeg全路径

    private static final String FFMPEG_PATH = "E:\\ffmpeg\\bin\\ffmpeg.exe";


    private static int count = 1;
    /**
     * 具体合成视频函数
     *
     * @param videoInputPath 原视频的全路径
     * @param audioInputPath 音频的全路径
     * @param videoOutPath   视频与音频结合之后的视频的路径
     */
    public static void convetor(String videoInputPath, String audioInputPath, String videoOutPath) throws Exception {

        Date startTime = DateUtil.date();
        System.out.println("开始合并第"+(count++)+"个视频");

        Process process = null;

        InputStream errorStream = null;

        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;


        try {

            // ffmpeg命令
            String command = FFMPEG_PATH + " -i " + videoInputPath + " -i " + audioInputPath
                    + " -c:v copy -c:a aac -strict experimental " +

                    " -map 0:v:0 -map 1:a:0 "

                    + " -y " + videoOutPath;

            process = Runtime.getRuntime().exec(command);

            errorStream = process.getErrorStream();
            inputStreamReader = new InputStreamReader(errorStream);
            br = new BufferedReader(inputStreamReader);

           /* // 用来收集错误信息的
            String str = "";
            while ((str = br.readLine()) != null) {
                System.out.println(str);
            }
*/
            process.waitFor();

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            if (br != null) {

                br.close();

            }

            if (inputStreamReader != null) {

                inputStreamReader.close();

            }

            if (errorStream != null) {

                errorStream.close();

            }
        }
        Date endTime = DateUtil.date();
        long interval = DateUtil.between(startTime, endTime, DateUnit.SECOND);
        System.out.println("合并完成，共花费"+interval+"秒");
    }




    public static void getAll(String path) throws Exception {

        String videoInputPath = "";
        String audioInputPath = "";
        String videoOutPath = "";

        File file = new File(path);     //资源文件所在路径
        /*
            第一层循环，打开一个视频系列

            第二层循环，打开一个分p，并且读取出分p名称

            第三层循环，打开音视频文件，获得路径，开始合并



         */


        if(file.isDirectory()){
            //来到根目录，获取各个系列视频

            File[] fileTopList = file.listFiles();

            for (File fileTop : fileTopList) {

                if (fileTop.isDirectory()){
                    //来到二级目录，拿到所有文件
                    //拿到entry.json
                    File[] fileTwoList = fileTop.listFiles();
                    for (File fileTwo : fileTwoList) {
                        if (fileTwo.getName().equals("entry.json")){

                        }
                    }

                }
            }

        }


/*
        if (file.isDirectory()) {       //如果是文件夹，就进行下一步遍历。如果不是就什么也不做


            File[] files = file.listFiles();//获取资源文件夹下的所有文件 、文件夹

            for (File f : files) {

                getAll(f.getPath());
                if (f.isFile()) {

                    if (f.getName().endsWith(".m4s")) {

                        if (f.getName().endsWith("audio.m4s"))
                            audioInputPath = file.getPath() + "\\audio.m4s";
                        if (f.getName().endsWith("video.m4s"))
                            videoInputPath = file.getPath() + "\\video.m4s";
                        videoOutPath = file.getPath() + "\\all.mp4";


                        if (!videoInputPath.equals(""))
                            convetor(videoInputPath, audioInputPath, videoOutPath);

                    }

                }


            }

        }

*/

    }


    /**
     * 返回这个视频的标题
     * @param file
     * @return
     */
    public static String getVideoTitleFromJson(File file){
        String part =null;
        try {
            InputStream inputStream = new FileInputStream(file);
            EntryBean entryBean = (EntryBean)JSONObject.parseObject(inputStream, EntryBean.class);
            System.out.println(entryBean);
            part = entryBean.getPage_data().getPart();


        } catch (IOException e) {
            System.out.println("找不到这个文件");

            e.printStackTrace();

        }
        return part;
    }
}