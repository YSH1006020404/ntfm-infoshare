package cn.les.ntfm.infoshare.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @create 2019-10-12-下午4:49
 */
@Data
public class CheckConnDo implements Comparable {
    private String host;
    private int conn;
    private Date checkTime;
    private String oper;



    @Override
    public int compareTo(Object o) {

        CheckConnDo checkConnDo = (CheckConnDo) o;
        if (this.conn > checkConnDo.conn) {
            return 1;
        } else if (this.conn < checkConnDo.conn) {
            return -1;
        } else {
            return 0;
        }
    }
}
