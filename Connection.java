import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Connection{
    final int PORT = 5000;
    private InetAddress addr;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Connection(String host_name){
        //接続の確立
        try {
            addr = InetAddress.getByName(host_name);

            socket = new Socket(addr, PORT);

            //入出力用ストリームの用意
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //選択と掛け金を送信
    public void sendChoice(boolean choice, int bet){
        out.println(choice);
        out.println(bet);
    }

    //文字列の送信
    public void sendStr(String str){
        out.println(str);
    }

    //送信されてきた選択と掛け金を受け取る
    public void receiveChoice(boolean choice, int bet){
        try {
            choice = Boolean.parseBoolean( in.readLine() );
            bet    = Integer.parseInt( in.readLine() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //文字列の受け取り
    public void receiveStr(String str){
        try {
            str = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //接続の終了処理
    public void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
