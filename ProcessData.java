package queuedserver;

import static queuedserver.QueuedServer.avg;
import static queuedserver.QueuedServer.count;
import static queuedserver.QueuedServer.delay;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chalil
 */
public class ProcessData {
    int cnt = 0;
    
    public void processData(ClientObj c){
        try {
            long request,response;
            ByteBuffer writeBuff = ByteBuffer.allocate(512);
            SelectionKey key = c.getKey();
            String data = c.getData();
            if(!key.isValid())
                return;
            SocketChannel channel = (SocketChannel) key.channel();
            String f;
            request = System.currentTimeMillis() % 1000;
            if(data == null ) {
                System.out.println("Closing connection..."+channel.socket().getPort());
                key.channel().close();
                key.cancel();
                return;
            }
            //process accepted data and write response to client
            if(!data.isEmpty()) {
                if(data.startsWith(" :")) {
                    f = data;
                }
                else {
                switch (data) {
                    case "D":
                        f = "Login Unsuccessfull";
                        break;
                    case "S":
                        f =  "Login Successfull";
                        break;
                    default:
                        Calculate calc =new Calculate();
                        f = calc.Calculate(data);
                        break;
                }
                }
                writeBuff.clear();
                writeBuff.put(f.getBytes());
                writeBuff.put(new byte[writeBuff.capacity() - f.getBytes().length]);
                writeBuff.flip();
                try {
                channel.write(writeBuff);
                } catch(IOException e) {
                    key.channel().close();
                    key.cancel();
                    return;    
                }

                response = System.currentTimeMillis() % 1000;
                delay = response-request;
                avg = (avg*count +delay)/(count++);
                System.out.println("Average time delay..."+(avg)+"ms");
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}