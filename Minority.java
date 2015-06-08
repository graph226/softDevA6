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
	ArrayList<Connection> connect;	//接続の配列

	//コンストラクタ
	public Minority(int pCount){
		ACount = 0;
		BCount = 0;
		this.pCount = pCount;
		betSum = 0;
		svSocket = null;
		connect = new ArrayList<Connection>();
	}

	//文字列をクライアント全体に送信
    synchronized public void broadcastStr(String msg){
    	for(Iterator<Connection> it = connect.iterator(); it.hasNext(); ){
    		Connection c = it.next();
    		c.sendStr(msg);
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
		if(ACount < BCount){
			if(ACount == BCount) return 0;
			else return 1;
		}
		return -1;
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
		System.out.println("input the number of player : ");
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
			System.out.println(i + " players connected.");
		}

		System.out.println("all connection established.");

		//メインループ
		while(true){
			//問題文の入力,送信
			System.out.println("input question : ");
			try{
				server.setQueStr( in.readLine() );
			}catch (IOException e) {
				e.printStackTrace();
			}
			server.broadcastStr( server.getQueStr() );

			//投票の開始
			for(Iterator<Connection> it = server.connect.iterator(); it.hasNext();){
				Connection c = it.next();
				c.setMode("vote");
				c.start();
			}

			server.joinALLConnection();

			//投票結果の送信。賞金は勝利人数で山分け
			int result = server.judge();
			String resultStr = result > 0 ? "yes\n" : result != 0 ? "no\n" : "same\n";
			int winCount = result > 0 ? server.ACount : server.BCount;
			if( winCount == 0 ) winCount++;
			int prize = result > 0 ? server.betSum/winCount : server.betSum/winCount;
			server.broadcastStr( resultStr + prize );

			//継続判定の開始
			server.broadcastStr( "" + server.pCount );

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
				server.broadcastStr("END");

				//残ったコネクションをすべて閉じる
				for(int i = 0; i < server.connect.size(); i++){
					Connection c = server.connect.get(i);
					c.close();
					server.connect.remove(c);
					i--;
				}
				System.out.println("end game Server");
				break;
			}else{
				server.broadcastStr("next turn.");
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
