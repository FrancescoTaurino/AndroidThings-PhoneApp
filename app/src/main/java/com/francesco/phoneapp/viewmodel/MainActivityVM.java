package com.francesco.phoneapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivityVM extends ViewModel {
    private MutableLiveData<LinkedHashMap<String, Message>> messagesMap;

    public LiveData<LinkedHashMap<String, Message>> getLiveDataMessagesMap() {
        if (messagesMap == null) {
            messagesMap = new MutableLiveData<>();
            messagesMap.setValue(new LinkedHashMap<String, Message>());
        }

        return messagesMap;
    }

    public int getMessagesMapSize() {
        return this.messagesMap.getValue().size();
    }

    public String getKeyFromMessagesMapByIndex(int index) {
        LinkedHashMap<String, Message> actualMessagesMap = this.messagesMap.getValue();
        ArrayList<String> keys = new ArrayList<>(actualMessagesMap.keySet());
        return keys.get(index);
    }

    public Message getMessageFromMessagesMapByIndex(int index) {
        LinkedHashMap<String, Message> actualMessagesMap = this.messagesMap.getValue();
        ArrayList<Message> messages = new ArrayList<>(actualMessagesMap.values());
        return messages.get(index);
    }

    public void addMessage(String key, Message message) {
        LinkedHashMap<String, Message> actualMessagesMap = this.messagesMap.getValue();
        actualMessagesMap.put(key, message);
        messagesMap.setValue(actualMessagesMap);
    }

    public void updateMessage(String key, Message message) {
        addMessage(key, message);
    }

    public void removeMessage(String key) {
        LinkedHashMap<String, Message> actualMessagesMap = this.messagesMap.getValue();
        actualMessagesMap.remove(key);
        messagesMap.setValue(actualMessagesMap);
    }

    public boolean messagesMapIsFull() {
        LinkedHashMap<String, Message> actualMessagesMap = this.messagesMap.getValue();

        int ctr = 0;
        for (Message message: actualMessagesMap.values()) {
            if (!message.isSeen())
                ctr++;

            if (ctr == 3)
                return true;
        }

        return false;
    }
}
