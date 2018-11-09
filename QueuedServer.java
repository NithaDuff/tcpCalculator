/*Scalable Server that uses multiple thread as available to connect to the required number of clients*/
package queuedserver;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author chalil
 */


public class QueuedServer {
    
    static final int  MAX_PROCESSOR = Runtime.getRuntime().availableProcessors() - 1;
    public static long count=1,avg =0,delay;
    public static Selector selector;
    static HashMap<SocketChannel,Boolean> map = new HashMap<>();
    private static final BlockingQueue<ClientObj> primeQ = new ArrayBlockingQueue<>(20);
    private static final BlockingQueue<ClientObj> freeQ = new ArrayBlockingQueue<>(20);
    static RSA rsa = new RSA();
    public static BigInteger pubKey[] = rsa.getPubKey();
    
    public static void main(String args[]) throws IOException, InterruptedException, NoSuchAlgorithmException {
        ServerSocketChannel server = ServerSocketChannel.open();
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, MAX_PROCESSOR-1));
        selector = Selector.open();
        InetSocketAddress socket = new InetSocketAddress("localhost", 9999);
        
        //configure server
        server.configureBlocking(false);
        server.socket().bind(socket);
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server Started...!");
        
        //prime executor
        for(int i=0;i < MAX_PROCESSOR-1;i++) {        
            executor.execute(() -> {
                try {
                    while(true) {
                        ClientObj c = primeQ.take();
                        ProcessData pd = new ProcessData();
                        pd.processData(c);
                        Thread.sleep(5);
                    }
                }catch(InterruptedException e){e.printStackTrace(System.out);}
            });
        }
        
        //free executor
        Thread free = new Thread(() -> {
            try {
                while(true) {
                    ClientObj c = freeQ.take();
                    ProcessData pd = new ProcessData();
                    pd.processData(c);
                    Thread.sleep(5);
                }
            }catch(InterruptedException e){e.printStackTrace(System.out);}
        });
                
        free.setPriority(Thread.MIN_PRIORITY);
        free.start();        
        Object obj = new Object();
        
        
        //incoming connections from clients
        while (true) {
            synchronized(obj) {
                while(selector.select() == 0) 
                    obj.wait();
                obj.notify();
            }
            Set<SelectionKey> key = selector.selectedKeys();
            Iterator<SelectionKey> iterator = key.iterator();
            while (iterator.hasNext()) {
                SelectionKey myKey = iterator.next();
                iterator.remove();
                if (!myKey.isValid())
                    continue;
                if(myKey.isAcceptable()) {
                    SocketChannel client = server.accept();
                    if(client != null) {
                        Socket clientSock = client.socket();
                        client.configureBlocking(false);
                        
                        //queue prime clients
                        if (clientSock.getPort()%2 == 0) { 
                            System.out.println("Premium connection Accepted: " + clientSock.getPort());
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,"Prime");
                        }
                        //queue free clients
                        else { 
                            System.out.println("\t\t\tFree Connection Accepted: " + clientSock.getPort());
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE,"Free");
                        }
                    }
                }
                else if(myKey.isReadable()) {
                    readData(myKey);
                }
            }
        }
    }

    private static void readData(SelectionKey myKey) throws IOException, InterruptedException, NoSuchAlgorithmException {
        int res;
        String data;
        SocketChannel client = (SocketChannel) myKey.channel();
        ByteBuffer readBuff = ByteBuffer.allocate(512);
        
        if(!myKey.isValid())
            return;
        readBuff.clear();
        try {
            //read request from client
            res = client.read(readBuff);
            data = new String(readBuff.array());
            if(res == -1) 
                 enqueue(null, myKey);
            else {
                if(data.trim().equals("Ping")) {
                    //send public key to client
                    enqueue(" : "+String.valueOf(pubKey[0])+" : "+String.valueOf(pubKey[1])+" : ",myKey);
                    Thread.sleep(500);
                }
                else if(data.startsWith(" :")){ 
                    if(!map.containsKey(client))
                    authenticate(data,myKey);
                }
                else 
                    enqueue(data.trim(), myKey);
            }
        }catch(IOException e) {
            myKey.cancel();
            client.close();
        }
    }

    private static void authenticate(String data, SelectionKey myKey) throws IOException, InterruptedException, NoSuchAlgorithmException {

        ByteBuffer readBuff = ByteBuffer.allocate(512);
        SocketChannel client = (SocketChannel) myKey.channel();
        if(!myKey.isValid())
            return;
        readBuff.clear();
        client.read(readBuff);
        //accept ecrypted code
        if(data.startsWith(" :")) {
            String[] cred = data.split(" : ");
            BigInteger id = new BigInteger(cred[1]);
            //decrypt code using private key
            BigInteger code = rsa.decrypt(id);
            BigInteger pass =new BigInteger("12345");//sample passcode
            if(code.compareTo(pass) == 0) {
                System.out.println("Client Successfully logged in...");
                map.put(client, true);
                enqueue("S", myKey);
            } else {
                System.out.println("Invalid Login credentials");
                map.put(client, false);
                enqueue("D", myKey);
                Thread.sleep(1000);
                myKey.cancel();
                client.close();
            }
        }
        else {
            System.out.println("Login credentials not recieved...");
            map.put(client, false);
            enqueue("D", myKey);
            Thread.sleep(1000);
            myKey.cancel();
            client.close();
        }
    }

    private static void enqueue(String data, SelectionKey myKey) throws InterruptedException {
        if(myKey.attachment().equals("Prime"))
                primeQ.put(new ClientObj(data, myKey));
            else
                freeQ.put(new ClientObj(data, myKey));
    }
}