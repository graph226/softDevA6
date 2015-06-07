import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Connection extends Thread{
    final int PORT = 5050;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public boolean isAlive;
    private char modeFlag;  //通信モードの設定
    String handle;          //クライアントのハンドル名。サーバーなら"server"
    Minority mServer;       //ゲームサーバーのインスタンス。クライアントならnull
    User user;              //接続しているプレイヤーのインスタンス。サーバーならnull。
    static final int VOTE    = 1 << 0;
    static final int INTERIM = 1 << 1;
    static final int DISCUSS = 1 << 2;

    //クライアントからホスト名を指定して接続
    public Connection(String host_name, String handle, User user){
        //接続の確立
        try {
            InetAddress addr = InetAddress.getByName(host_name);

            socket = new Socket(addr, PORT);
            //入出力用バッファの作成
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

            this.handle = handle;
            mServer = null;
            isAlive = true;
            this.user = user;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //サーバーからソケットをもらって接続
    public Connection(Socket socket, Minority server){
        this.socket = socket;
        handle = "server";
        mServer = server;
        user = null;
        isAlive = true;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setMode(String mode){
        switch(mode){
            case "vote":
                modeFlag = VOTE;
                break;

            case "interim":
                modeFlag = INTERIM;
                break;

            case "discuss":
                modeFlag = DISCUSS;
                break;
        }
    }

    //文字列の送信
    public void sendStr(String str){
        out.println(str);
    }


    //文字列の受け取り
    public String receiveStr(){
        String tmp = null;
        try {
            tmp = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    //接続の終了処理
    public void close(){
        try {
            in.close();
            out.close();
            socket.close();
            isAlive = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ほとんどここで処理してる
    //触る時は注意
    public void run(){
        //サーバが持つコネクションの処理
        if(mServer != null){
            //投票受付の処理
            if(modeFlag == VOTE){
                String tmp = receiveStr();
                if( tmp.equals("yes") ){
                    tmp = receiveStr();
                    mServer.acceptVote( true,  Integer.parseInt(tmp) );
                }else{
                    tmp = receiveStr();
                    mServer.acceptVote( false, Integer.parseInt(tmp) );
                }
            }

            //途中経過表示処理
            if(modeFlag == INTERIM){
                //ハンドル名取得
                String handle = receiveStr();
                //所持金取得
                int money = Integer.parseInt( receiveStr() );
                if(money > 0){
                    mServer.broadcastStr( handle + " : $" + money );
                }else{
                    //金額が0以下なら脱落
                    this.isAlive = false;
                    mServer.broadcastStr( handle + " : DROPPED OUT" );
                }
            }

        }else{
            //クライアントが持つコネクションの処理
            if(modeFlag == VOTE){
                //投票内容をもらって送信
                String input = user.inputChoice();
                this.sendStr(input);
            }

            if(modeFlag == INTERIM){
                sendStr( user.getName()  );
                sendStr( "" + user.getMoney() );

                //全プレイヤーの金額を受け取る
                int pCount = Integer.parseInt( receiveStr() );

                for(int i = pCount; i == 0; i--){
                    System.out.println( receiveStr() );
                }
            }
        }
    }
}
