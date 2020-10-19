package dis.countries.chat;

public class Item {

    final String nickname;
    final String message;
    final String msgType;

    public Item(String nickname, String message, String msgType){
        this.nickname = nickname;
        this.message = message;
        this.msgType = msgType;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public boolean getIsAnnouncement() {
        return msgType.equals("announcement");
    }
}