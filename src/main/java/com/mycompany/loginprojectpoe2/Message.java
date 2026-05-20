package com.mycompany.loginprojectpoe2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Message class for handling messaging functionality
 * 
 * @author Student Name
 * @version 2.0
 * @date 2026-05-20
 */
public class Message {
    
    private String messageId;
    private int messageNumber;
    private String recipientNumber;
    private String messageContent;
    private String messageHash;
    private String status;
    private long timestamp;
    
    private static int totalMessagesSent = 0;
    private static int totalMessagesStored = 0;
    private static List<Message> allMessages = new ArrayList<>();
    private static Random random = new Random();
    private Login loginValidator;
    
    private static final String JSON_FILE = "messages.json";
    private static final int MAX_MESSAGE_LENGTH = 250;
    
    public Message() {
        this.loginValidator = new Login();
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }
    
    public Message(String recipientNumber, String messageContent) {
        this.recipientNumber = recipientNumber;
        this.messageContent = messageContent;
        this.loginValidator = new Login();
        generateMessageId();
        this.messageNumber = totalMessagesSent + totalMessagesStored + 1;
        createMessageHash();
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean checkMessageID() {
        if (messageId == null) {
            return false;
        }
        return messageId.length() <= 10;
    }
    
    public void generateMessageId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            id.append(random.nextInt(10));
        }
        this.messageId = id.toString();
    }
    
    public String checkRecipientCell() {
        if (recipientNumber == null || recipientNumber.isEmpty()) {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
        
        if (loginValidator.checkCellPhoneNumber(recipientNumber)) {
            return "Cell phone number successfully captured.";
        } else {
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        }
    }
    
    public String createMessageHash() {
        if (messageId == null || messageId.length() < 2) {
            generateMessageId();
        }
        
        String firstTwoDigits = messageId.substring(0, 2);
        String[] words = messageContent.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 0 ? words[words.length - 1] : "";
        
        firstWord = firstWord.replaceAll("[^a-zA-Z]", "");
        lastWord = lastWord.replaceAll("[^a-zA-Z]", "");
        
        this.messageHash = firstTwoDigits + ":" + messageNumber + ":" + 
                          (firstWord + lastWord).toUpperCase();
        
        return this.messageHash;
    }
    
    public String validateMessageContent() {
        if (messageContent.length() <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        } else {
            int excess = messageContent.length() - MAX_MESSAGE_LENGTH;
            return "Message exceeds " + MAX_MESSAGE_LENGTH + " characters by " + excess + "; please reduce the size.";
        }
    }
    
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                this.status = "sent";
                totalMessagesSent++;
                return "Message successfully sent.";
            case 2:
                this.status = "stored";
                totalMessagesStored++;
                return "Message successfully stored.";
            case 3:
                this.status = "disregarded";
                return "Press 0 to delete the message.";
            default:
                return "Invalid option selected. Please choose 1, 2, or 3.";
        }
    }
    
    // ========== ALIAS METHODS FOR TEST COMPATIBILITY ==========
    
    public String validateRecipientCell() {
        return checkRecipientCell();
    }
    
    public String processMessageAction(int choice) {
        return sentMessage(choice);
    }
    
    public void generateMessageHash() {
        createMessageHash();
    }
    
    public static void resetCounter() {
        resetAll();
    }
    
    // ========== END OF ALIAS METHODS ==========
    
    public static String printMessages() {
        if (allMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        
        StringBuilder output = new StringBuilder();
        output.append("\n" + "=".repeat(70) + "\n");
        output.append(String.format("%-15s %-10s %-20s %-15s %-15s\n", 
                      "Message ID", "#", "Recipient", "Hash", "Status"));
        output.append("=".repeat(70) + "\n");
        
        for (Message msg : allMessages) {
            String shortHash = msg.messageHash.length() > 15 ? 
                               msg.messageHash.substring(0, 12) + "..." : 
                               msg.messageHash;
            output.append(String.format("%-15s %-10d %-20s %-15s %-15s\n", 
                          msg.messageId, 
                          msg.messageNumber, 
                          msg.recipientNumber, 
                          shortHash,
                          msg.status));
        }
        output.append("=".repeat(70) + "\n");
        
        return output.toString();
    }
    
    public static String printDetailedMessages() {
        if (allMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        
        StringBuilder output = new StringBuilder();
        output.append("\n=== ALL MESSAGES ===\n");
        for (Message msg : allMessages) {
            output.append("────────────────────────────────────────────────────────────────\n");
            output.append(" Message ID    : ").append(msg.messageId).append("\n");
            output.append(" Message #     : ").append(msg.messageNumber).append("\n");
            output.append(" Recipient     : ").append(msg.recipientNumber).append("\n");
            output.append(" Message       : ").append(msg.messageContent).append("\n");
            output.append(" Message Hash  : ").append(msg.messageHash).append("\n");
            output.append(" Status        : ").append(msg.status).append("\n");
            output.append("────────────────────────────────────────────────────────────────\n");
        }
        return output.toString();
    }
    
    public static int returnTotalMessages() {
        return totalMessagesSent;
    }
    
    public static int returnTotalStoredMessages() {
        return totalMessagesStored;
    }
    
    public static int returnTotalAllMessages() {
        return allMessages.size();
    }
    
    public static void storeMessage() {
        JSONArray jsonArray = new JSONArray();
        
        for (Message msg : allMessages) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("messageId", msg.messageId);
            jsonObj.put("messageNumber", msg.messageNumber);
            jsonObj.put("recipientNumber", msg.recipientNumber);
            jsonObj.put("messageContent", msg.messageContent);
            jsonObj.put("messageHash", msg.messageHash);
            jsonObj.put("status", msg.status);
            jsonObj.put("timestamp", msg.timestamp);
            jsonArray.put(jsonObj);
        }
        
        try (FileWriter file = new FileWriter(JSON_FILE)) {
            file.write(jsonArray.toString(4));
            System.out.println(" Messages successfully stored to " + JSON_FILE);
        } catch (IOException e) {
            System.out.println(" Error storing messages: " + e.getMessage());
        }
    }
    
    public static void loadMessages() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(JSON_FILE)));
            JSONArray jsonArray = new JSONArray(content);
            
            allMessages.clear();
            totalMessagesSent = 0;
            totalMessagesStored = 0;
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                Message msg = new Message();
                msg.messageId = jsonObj.getString("messageId");
                msg.messageNumber = jsonObj.getInt("messageNumber");
                msg.recipientNumber = jsonObj.getString("recipientNumber");
                msg.messageContent = jsonObj.getString("messageContent");
                msg.messageHash = jsonObj.getString("messageHash");
                msg.status = jsonObj.getString("status");
                msg.timestamp = jsonObj.optLong("timestamp", System.currentTimeMillis());
                allMessages.add(msg);
                
                if (msg.status.equals("sent")) {
                    totalMessagesSent++;
                } else if (msg.status.equals("stored")) {
                    totalMessagesStored++;
                }
            }
            System.out.println("✓ Messages loaded from " + JSON_FILE);
        } catch (IOException e) {
            System.out.println("ℹ No existing messages file found. Starting fresh.");
        }
    }
    
    public static void addMessage(Message message) {
        allMessages.add(message);
        storeMessage();
    }
    
    public static Message[] getAllMessagesArray() {
        return allMessages.toArray(new Message[0]);
    }
    
    public static Message getMessageById(String messageId) {
        for (Message msg : allMessages) {
            if (msg.messageId.equals(messageId)) {
                return msg;
            }
        }
        return null;
    }
    
    public static Message getMessageByHash(String messageHash) {
        for (Message msg : allMessages) {
            if (msg.messageHash.equals(messageHash)) {
                return msg;
            }
        }
        return null;
    }
    
    public static boolean deleteMessageById(String messageId) {
        for (int i = 0; i < allMessages.size(); i++) {
            if (allMessages.get(i).messageId.equals(messageId)) {
                allMessages.remove(i);
                storeMessage();
                return true;
            }
        }
        return false;
    }
    
    public static void resetAll() {
        totalMessagesSent = 0;
        totalMessagesStored = 0;
        allMessages.clear();
    }
    

    
    public String getMessageId() { 
        return messageId; 
    }
    
    public void setMessageId(String messageId) { 
        this.messageId = messageId; 
    }
    
    public int getMessageNumber() { 
        return messageNumber; 
    }
    
    public void setMessageNumber(int messageNumber) { 
        this.messageNumber = messageNumber; 
    }
    
    public String getRecipientNumber() { 
        return recipientNumber; 
    }
    
    public void setRecipientNumber(String recipientNumber) { 
        this.recipientNumber = recipientNumber; 
    }
    
    public String getMessageContent() { 
        return messageContent; 
    }
    
    public void setMessageContent(String messageContent) { 
        this.messageContent = messageContent; 
    }
    
    public String getMessageHash() { 
        return messageHash; 
    }
    
    public void setMessageHash(String messageHash) { 
        this.messageHash = messageHash; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public long getTimestamp() { 
        return timestamp; 
    }
    
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
}