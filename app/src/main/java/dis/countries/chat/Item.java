package dis.countries.chat;

import android.graphics.drawable.Drawable;

public class Item {

    final String nickname;
    final String message;
    private String msgType;
    private String messageStatus;

    public Item(String nickname, String message, String msgType){
        this.nickname = nickname;
        this.message = message;
        this.msgType = msgType;
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

    public boolean getIsAnnouncement() {
        return msgType.equals("announcement");
    }

    public String getMsgType(){
        return msgType;
    }

    public void setMsgData(String s) {
        this.msgType = s;
    }

    public int getMessageStatus() {
        if (messageStatus.equals("sending")){
            return R.drawable.ic_baseline_access_time_24;
        }
        else
            return R.drawable.ic_baseline_check_24;
    }
}
