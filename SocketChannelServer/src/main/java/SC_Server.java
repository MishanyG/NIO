
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class SC_Server {

    public static void main (String[] args) throws IOException {
        Charset charset = Charset.forName (System.getProperty ("file.encoding"));
        ByteBuffer buffer = ByteBuffer.allocate (256);
        SocketChannel channel = null;
        ServerSocketChannel serverChannel = ServerSocketChannel.open ();
        Selector selector = Selector.open ();
        try {
            serverChannel.configureBlocking (false);
            serverChannel.socket ().bind (new InetSocketAddress (8189));
            serverChannel.register (selector, SelectionKey.OP_ACCEPT);
            System.out.println ("Server on port: " + 8189);
            int i = 0;
            while (true) {
                selector.select ();
                Iterator <SelectionKey> it = selector.selectedKeys ().iterator ();
                while (it.hasNext ()) {
                    SelectionKey key = it.next ();
                    it.remove ();
                    if (key.isAcceptable ()) {
                        channel = serverChannel.accept ();
                        System.out.println ("Accepted connection from:" + channel.socket ());
                        i++;
                        channel.configureBlocking (false);
                        channel.register (selector, SelectionKey.OP_READ, "User " + i + ": ");
                    } else {
                        try {
                            channel = (SocketChannel) key.channel ();
                            channel.read (buffer);
                            CharBuffer charBuffer = charset.decode ((ByteBuffer) buffer.flip ());
                            String response = charBuffer.toString ();
                            System.out.print (key.attachment () + response);
                            channel.write ((ByteBuffer) buffer.rewind ());
                            buffer.clear ();
                        } catch (IOException e) {
                            System.out.println ("Client: " + key.attachment () + " disconnected!");
                            channel.close ();
                        }
                    }
                }
            }
        } finally {
            if (channel != null)
                channel.close ();
            serverChannel.close ();
            selector.close ();
        }
    }
}