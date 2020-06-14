import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class RecordDTO implements Comparable<RecordDTO> {
    private Long orderid;
    private Long shopid;
    private Long userid;
    private Date event_time;

    public Long getOrderId() {
        return orderid;
    }

    public void setOrderId(Long orderid) {
        this.orderid = orderid;
    }

    public Long getShopId() {
        return shopid;
    }

    public void setShopId(Long shopid) {
        this.shopid = shopid;
    }

    public Long getUserId() {
        return userid;
    }

    public void setUserId(Long userid) {
        this.userid = userid;
    }

    public Date getEventTime() {
        return event_time;
    }

    public void setEventTime(Date event_time) {
        this.event_time = event_time;
    }

    @Override
    public int compareTo(RecordDTO u) {
        if (getEventTime() == null || u.getEventTime() == null) {
            return 0;
          }
          return getEventTime().compareTo(u.getEventTime());
    }

    public int getHour() {
        return LocalDateTime.ofInstant(event_time.toInstant(), ZoneId.systemDefault()).getHour();
    }
}