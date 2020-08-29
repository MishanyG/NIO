import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class SC_Client {
    public static void main (String[] args) throws IOException {
        Selector selector = Selector.open ();
        SocketChannel channel = SocketChannel.open ();
        BufferedReader console = new BufferedReader (new InputStreamReader (System.in));
        channel.configureBlocking (false);
        boolean success = channel.connect (new InetSocketAddress ("localhost", 8189));
        try {
            channel.register (selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
            boolean written = false, done = false;
            String encoding = System.getProperty ("file.encoding");
            Charset charset = Charset.forName (encoding);
            ByteBuffer buf = ByteBuffer.allocate (256);
            if (channel.isRegistered ()) System.out.print ("Connection is established!\n");
            while (! done) {
                selector.select ();
                Iterator <SelectionKey> iterator = selector.selectedKeys ().iterator ();
                while (iterator.hasNext ()) {
                    SelectionKey key = iterator.next ();
                    iterator.remove ();
                    channel = (SocketChannel) key.channel ();
                    if (key.isConnectable () && ! channel.isConnected ())
                        if (! success)
                            channel.finishConnect ();
                    if (key.isReadable () && written) {
                        try {
                            if (channel.read ((ByteBuffer) buf.clear ()) > 0) {
                                written = false;
                                String response = "Echo: " + charset.decode ((ByteBuffer) buf.flip ()).toString ();
                                System.out.print (response);
                            }
                        } catch (IOException e) {
                            done = true;
                        }
                    }
                    if (key.isWritable () && ! written) {
                        channel.write (ByteBuffer.wrap ((console.readLine () + '\n').getBytes ()));
                        written = true;
                    }
                }
            }
        } finally {
            channel.close ();
            selector.close ();
        }
    }
}