import java.io.*;
import java.net.*;


public class JabberServer {
	public static void main(String[] args) 
		throws IOException{
		Integer.parseInt(args[0]);
		ServerSocket s = new ServerSocket(Integer.parseInt(args[0]));
		System.out.println("Started: " + s);
		try {
			Socket socket = s.accept();
			try {
				System.out.println("Connection accepted:" + socket);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
				while(true){
					String str = in.readLine();
  					if(str.equals("END")) break;
  					System.out.println("Echoing : ");
  					out.println(str);
				}
			}finally{
				System.out.println("closing...");
				socket.close();
				
			}
		}finally{
			s.close();
		}
		

	}

}

