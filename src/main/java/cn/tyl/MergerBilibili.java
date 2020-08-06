package cn.tyl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.tyl.entity.ConfigProperties;
import cn.tyl.entity.EntryBean;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MergerBilibili {

    private static ConfigProperties configProperties;
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    static {


        //关联文件
        try {
            InputStream inputStream = MergerBilibili.class.getClass().getResourceAsStream("/config.json");
            configProperties = (ConfigProperties) JSONObject.parseObject(inputStream, ConfigProperties.class);
        } catch (FileNotFoundException e) {
            System.out.println("配置文件找不到");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("配置文件找不到");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String path = "";
        if (args.length>0){
            if(!args[0].equals("")||args[0]!=null) {


                path = args[0];
                System.out.println(path);
            }
        }else {
            path = configProperties.getRootPath();
        }






        try {
            getAll(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }






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
        System.out.println("开始合并第" + (count++) + "个视频");

        Process process = null;

        InputStream errorStream = null;

        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;


        try {

            // ffmpeg命令
            String command = "ffmpeg" + " -i " + videoInputPath + " -i " + audioInputPath
                    + " -c:v copy -c:a aac -strict experimental " +

                    " -map 0:v:0 -map 1:a:0 "

                    + " -y " + videoOutPath;

            process = Runtime.getRuntime().exec(command);

            errorStream = process.getErrorStream();
            inputStreamReader = new InputStreamReader(errorStream);
            br = new BufferedReader(inputStreamReader);

            // 用来收集错误信息的
            String str = "";
            while ((str = br.readLine()) != null) {
                System.out.println(str);
            }
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
        System.out.println("合并完成，共花费" + interval + "秒");
    }


    /**
     * 获取路径
     *
     * @param path
     * @throws Exception
     */
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


        if (file.isDirectory()) {
            //来到根目录，获取各个系列视频
            File[] fileTopList = file.listFiles();

            loop01:
            for (File fileTop : fileTopList) {

                if (fileTop.isDirectory()) {
                    String outputPath = getOutputPath(fileTop);
                    //来到一级目录
                    File[] fileOneList = fileTop.listFiles();
                    for (File fileOne : fileOneList) {

                        if (fileOne.isDirectory()) {

                            //来到二级目录，拿到所有文件
                            //拿到entry.json
                            File[] fileTwoList = fileOne.listFiles();
                            String videoTitle = "p" + fileOne.getName() + "-";


                            //因为要保证先获取到文件名，才能进入下一级文件。而遍历顺序无法保证，所有先拿到视频标题，再进入下一级文件
                            for (File fileTwo : fileTwoList) {
                                if (fileTwo.getName().equals("entry.json")) {
                                    String partName = getVideoTitleFromJson(fileTwo);
                                    partName = partName.replaceAll(" ", "_");
                                    videoTitle += partName;

                                    System.out.println(videoTitle);

                                }
                            }

                            //设置最终合并文件的位置
                            videoOutPath = outputPath + "\\" + videoTitle + ".mp4";


                            for (File fileTwo : fileTwoList) {
                                if (fileTwo.isDirectory()) {


                                    //来到三级目录，里面存放的是 audio.m4s 和 video.m4sk
                                    File[] fileThreeList = fileTwo.listFiles();

                                    for (File fileThree : fileThreeList) {

                                        if (fileThree.isFile()) {

                                            if (fileThree.getName().endsWith(".blv")) {


                                                FileChannel input = null;
                                                FileChannel output = null;


                                                input = new FileInputStream(fileThree).getChannel();
                                                output = new FileOutputStream(new File(videoOutPath)).getChannel();
                                                output.transferFrom(input, 0, input.size());


                                                continue loop01;
                                            }
                                            if (fileThree.getName().endsWith(".m4s")) {

                                                if (fileThree.getName().endsWith("audio.m4s"))
                                                    //设置音频文件的位置
                                                    audioInputPath = fileThree.getAbsolutePath();
                                                if (fileThree.getName().endsWith("video.m4s"))
                                                    //设置视频文件的位置
                                                    videoInputPath = fileThree.getAbsolutePath();
                                            }
                                        }

                                    }


                                    System.out.println("视频所在目录：" + audioInputPath);
                                    System.out.println("音频所在目录：" + videoInputPath);
                                    System.out.println("视频输出目录：" + videoOutPath);

                                    //开始合成
                                    if (!videoInputPath.equals("")){

                                        executorService.execute(new Runnable() {

                                            private String videoInputPath;
                                            private String audioInputPath;
                                            private String videoOutPath;

                                            @Override
                                            public void run() {
                                                try {
                                                    convetor(videoInputPath, audioInputPath, videoOutPath);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }


                                            public Runnable accept(String videoInputPath,String audioInputPath, String videoOutPath){

                                                this.videoInputPath =videoInputPath;
                                                this.videoOutPath = videoOutPath;
                                                this.audioInputPath = audioInputPath;
                                                return this;
                                            }


                                        }.accept(videoInputPath,audioInputPath,videoOutPath));


//
                                    }



                                }
                            }


                        }
                    }
                }
                System.out.println("----------------视频：" + fileTop.getName() + "已经遍历完成--------------------");
            }


        }
    }


    /**
     * 返回这个视频的标题
     *
     * @param file
     * @return
     */
    public static String getVideoTitleFromJson(File file) {
        String part = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            EntryBean entryBean = (EntryBean) JSONObject.parseObject(inputStream, EntryBean.class);

            part = entryBean.getPage_data().getPart();


        } catch (IOException e) {
            System.out.println("找不到这个文件");

            e.printStackTrace();

        }
        return part;
    }


    /**
     * 根据一级目录的路径，创建一个输出文件夹
     *
     * @param file
     * @return
     */
    public static String getOutputPath(File file) {

        String absolutePath = file.getAbsolutePath();
        absolutePath += "\\output";

        File outputPath = new File(absolutePath);
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }

        return absolutePath;
    }


}