package queuedserver;

import java.nio.channels.SelectionKey;

public class ClientObj {
        String data;
        SelectionKey key;

    ClientObj(String data, SelectionKey myKey) {
        this.data = data;
        this.key = myKey;
    }

        public String getData() {
            return data;
        }

        public SelectionKey getKey() {
            return key;
        }
    }