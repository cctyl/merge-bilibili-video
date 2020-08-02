package cn.tyl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntryBean {


    private int media_type;
    private boolean has_dash_audio;
    private boolean is_completed;
    private long total_bytes;
    private long downloaded_bytes;
    private String title;
    private String type_tag;
    private String cover;
    private int prefered_video_quality;
    private int guessed_total_bytes;
    private long total_time_milli;
    private int danmaku_count;
    private long time_update_stamp;
    private long time_create_stamp;
    private long avid;
    private int spid;
    private int seasion_id;
    private String bvid;
    private Page_data page_data;

}