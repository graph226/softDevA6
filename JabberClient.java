import java.io.*;
import java.net.*;

public class JabberClient {
	public static void main(String[] args) throws IOException {
		InetAddress addr = InetAddress.getByName("localhost");
		System.out.println("addr = " + addr);
		Socket socket = new Socket(addr, Integer.parseInt(args[0]));
		try {
			System.out.println("socket = " + socket);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
			for(int i = 0; i<10; i++){
				out.println("howdy" + i);
				String str = in.readLine();
				System.out.println(str);
			}
			out.println("END");
		}finally{
			System.out.println("closing...");
			socket.close();
		}
	}
}

