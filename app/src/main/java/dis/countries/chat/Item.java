package dis.countries.chat;


public class Item {

    final String nickname;
    final String message;
    private String msgType;
    private String messageStatus;
    private String time;
    private long timestamp;

    public Item(String nickname, String message, String msgType, long timestamp){
        this.nickname = nickname;
        this.message = message;
        this.msgType = msgType;
        this.timestamp = timestamp;
        this.time = Time.convertTime(timestamp);
        this.messageStatus = "";
    }


    public String getNickname() {
        return nickname;
    }

    public void setMessageStatus(String msg){
        messageStatus = msg;
    }
    public String getMessage() {
        return message;
    }

    public String getMsgType(){
        return msgType;
    }

    public void setMsgData(String s) {
        this.msgType = s;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public String getTime(){
        return time;
    }

    public int getDeleveryId() {
        switch (messageStatus) {
            case Parameters.DELIVERING_MSG:
                return R.drawable.ic_baseline_access_time_24;
            case Parameters.DELIVERED:
                return R.drawable.ic_baseline_check_24;
            case Parameters.DELIVERY_FAILED:
                return R.drawable.ic_baseline_error_24;
            default:
                return -1;
        }
    }
}
