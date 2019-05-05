package com.the_finder_group.tutorfinder.ConnManager;

public class UserMessageDTO {
    /**
     * Definicio dels atributs de l'objecte missatge
     * @author Jaime Mascuñan Xandro
     */

    //Atributs
    private int messageId;
    private int senderUserId;
    private String senderUserName;
    private String messageText;
    private String messageDate;
    private int receiverUserId;
    private String receiverUserName;

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageUserId() {
        return senderUserId;
    }

    public void setMessageUserId(int messageUserId) {
        this.senderUserId = messageUserId;
    }

    public String getMessageUserName() {
        return senderUserName;
    }

    public void setMessageUserName(String messageUserName) { this.senderUserName = messageUserName; }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getReceiverUserId() { return receiverUserId; }

    public void setReceiverUserId(int receiverUserId) { this.receiverUserId = receiverUserId; }

    public String getReceiverUserName() { return receiverUserName; }

    public void setReceiverUserName(String receiverUserName) { this.receiverUserName = receiverUserName; }

}
