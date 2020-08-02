package cn.tyl.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Page_data {

    private long cid;
    private int page;
    private String from;
    private String part;
    private String vid;
    private boolean has_alias;
    private int tid;
    private int width;
    private int height;
    private int rotate;


}