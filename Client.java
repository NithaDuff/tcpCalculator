package queuedserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author chalil
 */
public class Client {
    
    
    
    public static void main(String[] arg)throws Exception {        
        final AtomicInteger clients = new AtomicInteger(Integer.parseInt("0"));
        do {
            new Thread(() -> new ClientIn().send("Message:"+clients.get()) ).start();       
        }while(clients.getAndDecrement()> 0);  
    }   
   
    private static class ClientIn {
        
        static BigInteger N;
        static BigInteger e;
    
        public void send(String name){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                InetSocketAddress hostAddress = new InetSocketAddress("localhost", 9999);
                SocketChannel channel = SocketChannel.open(hostAddress);
                Socket s = channel.socket();
                ByteBuffer writeBuff = ByteBuffer.allocate(512);
                ByteBuffer readBuff = ByteBuffer.allocate(512);
                
                String pubKey[] = null;
                //greet server
                writeBuff.clear();
                writeBuff.put("Ping".getBytes());
                writeBuff.flip();
                channel.write(writeBuff);
                //recieve public key
                readBuff.clear();
                channel.read(readBuff);
                String data = new String(readBuff.array());
                if(data.startsWith(" :")) {
                    pubKey = data.split(" : ");
                    e = new BigInteger(pubKey[1]);
                    N = new BigInteger(pubKey[2]);
                }
                else {
                    System.out.println("key not recieved...");
                    System.exit(0);
                }
                //accept client passcode
                System.out.println("Enter key:");
                long id = Long.parseLong(br.readLine());
                System.out.print("\033[H\033[2J");  
                System.out.flush();  
                BigInteger value = BigInteger.valueOf(id);
                //ecrypt passcode
                BigInteger pass = encrypt(value);
                System.out.println("Client registered...");
                int res,i=5;
                //Send hashcode 
                writeBuff.clear();
                writeBuff.put((" : "+pass+" : ").getBytes());
                writeBuff.flip();
                channel.write(writeBuff);
                String eq;
                
                while(i-- > 0) {
                    long request = System.currentTimeMillis() % 1000;
                    //Reading response from server...
                    readBuff.clear();
                    res = channel.read(readBuff);
                    String val = new String(readBuff.array()).trim();
                    long response = System.currentTimeMillis() % 1000;
                    
                    if(res != -1)
                        System.out.println(val+"\t\t\t..."+(response-request)+"ms"+" ");
                    if(val.equals("Login Unsuccessfull"))
                        break;
//                    eq = br.readLine();
                    eq  = "5*6+2";
                    writeBuff.clear();
                    writeBuff.put((eq).getBytes());
                    writeBuff.put(new byte[writeBuff.capacity() - eq.getBytes().length-1]);
                    //Sending request to server...
                    writeBuff.flip();
                    channel.write(writeBuff);
                    if(eq.equals("#")) {
                        System.out.println("Disconnecting from server...");
                        break;
                    }
                }
                s.close();
            }catch(IOException exp){
                exp.printStackTrace(System.out);
            }
        }
        
        // Encrypt message
        public BigInteger encrypt(BigInteger message) {
            return ((message)).modPow(e, N);
        }
    }
}
