import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

public class Minority{
	private int ACount;			//Aを選んだ
	private int BCount;			//Bを選んだ
	private int pCount;			//プレイヤーの人数
	private int betSum;			//掛け金の合計
	private String queStr;		//問題文
	ServerSocket svSocket;
	ArrayList<Connection> connect;		//接続の配列
	ArrayList<Connection> chatSpeaker;	//チャット用の送信ソケット

	//コンストラクタ
	public Minority(int pCount){
		ACount = 0;
		BCount = 0;
		this.pCount = pCount;
		betSum = 0;
		svSocket = null;
		connect = new ArrayList<Connection>();
		chatSpeaker = new ArrayList<Connection>();
	}

	//文字列をクライアント全体に送信
    synchronized public void broadcastStr(String msg, int mode){
    	//チャット時はListenerに送信
    	if(mode == Connection.DISCUSS){
    		for(Iterator<Connection> it = chatSpeaker.iterator(); it.hasNext(); ){
	    		Connection c = it.next();
	    		c.sendStr(msg);
	    	}
    	}else{
    		//それ以外は通常のConnectionを利用
	    	for(Iterator<Connection> it = connect.iterator(); it.hasNext(); ){
	    		Connection c = it.next();
	    		c.sendStr(msg);
	    	}
    	}
    }

	//投票の受理
	synchronized public void acceptVote(boolean choice, int bet){
		if(choice){
			ACount++;
		}else{
			BCount++;
		}

		betSum += bet;
	}

	//Aが少数派なら正の数、Bが少数派なら負の数、同数なら0を返す
	public int judge(){
		if( ACount == BCount || ACount == 0 || BCount == 0 ) return 0;

		return ACount < BCount ? 1 : -1;
	}

	//すべてのコネクションが終了するのを待つ
    public void joinALLConnection(){
        for(int i = 0; i < pCount; i++ ){
        	try {
				connect.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    //負けたプレイヤーとの接続を切断する
    public void cutLosers(){
    	for(int i = 0; i < pCount; i++){
    		if( !connect.get(i).isAlive ){
    			connect.remove(i);
    			chatSpeaker.get(i).sendStr("END");
    			chatSpeaker.remove(i);
    			i--;
    			pCount--;
    		}
    	}
    }


    //問題文のsetter
    public String setQueStr(String queStr){
    	this.queStr = queStr;
    	return this.queStr;
    }

    //問題文のgetter
    public String getQueStr(){
    	return queStr;
    }

    public int getPCount(){
    	return pCount;
    }

    public static void main(String[] args){
		BufferedReader in = new BufferedReader( new InputStreamReader(System.in) );
		Minority server = null;

		//プレイヤーの人数決定
		System.out.println("Minority Server");
		System.out.print("input the number of player : ");
		try{
			server = new Minority( Integer.parseInt( in.readLine() ) );
		}catch (IOException e){
			e.printStackTrace();
		}
		//ServerSocketの作成
		try {
			server.svSocket = new ServerSocket(5050);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//接続受付開始
		System.out.println("connection waiting...");

		//接続してきた順にUserとのConnectionを作る
		for(int i = 0; i < server.pCount; i++){
			Connection con = null;

			try {
				con = new Connection( server.svSocket.accept(), server);
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.connect.add( con );
			System.out.println( (i+1) + " players connected.");
		}

		//順にUserとのチャット用Connectionを作る
		for(int i = 0; i < server.pCount; i++){
			Connection con = null;

			//順番に接続してもらってaccept()する
			server.connect.get(i).sendStr("come");
			try {
				con = new Connection( server.svSocket.accept(), server);
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.chatSpeaker.add( con );
			System.out.println( (i+1) + " players ready to start.");
		}

		System.out.println("all connection established.");



		//メインループ
		while(true){
			//問題文の入力,送信
			System.out.print("input question : ");
			try{
				server.setQueStr( in.readLine() );
			}catch (IOException e) {
				e.printStackTrace();
			}
			server.broadcastStr( server.getQueStr(), Connection.DEFAULT );

			//チャットの開始
			for(Iterator<Connection> it = server.connect.iterator(); it.hasNext();){
				Connection c = it.next();
				c.setMode("discuss");
				c.start();
			}

			//一分停止した後、チャット終了
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//終了メッセージを通常のConnectionに送る
			server.broadcastStr("quit_Chat", Connection.DISCUSS);

			server.joinALLConnection();

			//投票の開始
			for(int i = 0; i < server.connect.size(); i++){
				server.connect.set(i, new Connection( server.connect.get(i) ));
				server.connect.get(i).setMode("vote");
				server.connect.get(i).start();
			}

			server.joinALLConnection();

			//投票結果の送信。賞金は勝利人数で山分け
			int result = server.judge();
			String resultStr = result > 0 ? "yes\n" : result == 0 ? "same\n" : "no\n";
			int winCount = result > 0 ? server.ACount : server.BCount;
			if( winCount == 0 ) winCount++;
			int prize = result > 0 ? server.betSum/winCount : server.betSum/winCount;
			server.broadcastStr( resultStr + prize, Connection.DEFAULT);

			//継続判定の開始
			server.broadcastStr( "" + server.pCount, Connection.DEFAULT);

			for(int i = 0; i < server.connect.size(); i++){
				server.connect.set(i, new Connection( server.connect.get(i) ));
				server.connect.get(i).setMode("interim");
				server.connect.get(i).start();
			}

			server.joinALLConnection();

			//脱落したプレイヤーとの接続を切断
			server.cutLosers();

			//残りプレイヤーが2人以下なら終了処理
			if(server.pCount <= 2){
				server.broadcastStr("END", Connection.DEFAULT);
				server.broadcastStr("END", Connection.DISCUSS);

				//残ったコネクションをすべて閉じる
				for(int i = 0; i < server.connect.size(); i++){
					Connection c = server.connect.get(i);
					c.close();
					server.connect.remove(c);
					c = server.chatSpeaker.get(i);
					c.close();
					server.chatSpeaker.remove(c);
					i--;
				}
				System.out.println("end game Server");
				break;
			}else{
				server.broadcastStr("next turn.", Connection.DEFAULT);
			}

			//次のループのための準備
			for(int i = 0; i < server.connect.size(); i++){
				server.connect.set(i, new Connection( server.connect.get(i) ));
			}
			server.ACount = 0;
			server.BCount = 0;
			server.betSum = 0;
		}

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
