import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

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

    		if(c.isAlive){
    			c.sendStr(msg);
    		}else{
    			c.close();
    			connect.remove(c);
    			this.pCount--;
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

	//Aが少数派ならtrueを返す
	public boolean judge(){
		return ACount < BCount;
	}

	//すべてのコネクションが終了するのを待つ
    public void joinALLConnection(){
        for(Iterator<Connection> it = connect.iterator(); it.hasNext(); ){
        	try {
				it.next().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
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
		Scanner scanner = new Scanner(System.in);
		Minority server;

		//プレイヤーの人数決定
		System.out.println("Minority Server");
		System.out.println("input the number of player : ");
		int pCount = Integer.parseInt( scanner.next() );
		server = new Minority(pCount);

		//ServerSocketの作成
		try {
			server.svSocket = new ServerSocket(5050);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//接続受付開始
		System.out.println("connection waiting...");

		//接続してきた順にUserとのConnectionを作る
		for(int i = 0; i < pCount; i++){
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
			server.setQueStr( scanner.next() );

			server.broadcastStr( server.getQueStr() );

			//投票の開始
			for(Iterator<Connection> it = server.connect.iterator(); it.hasNext();){
				Connection c = it.next();
				c.setMode("vote");
				c.start();
			}

			server.joinALLConnection();

			//投票結果の送信。賞金は勝利人数で山分け
			boolean result = server.judge();
			String resultStr = result ? "yes\n" : "no\n";
			int prize = result ? server.betSum/server.ACount : server.betSum/server.BCount;
			server.broadcastStr( resultStr + prize );

			//継続判定の開始
			server.broadcastStr( "" + pCount );

			for(int i = 0; i < server.connect.size(); i++){
				server.connect.set(i, new Connection( server.connect.get(i) ));
				server.connect.get(i).setMode("interim");
				server.connect.get(i).start();
			}

			server.joinALLConnection();

			//残りプレイヤーが2人以下なら終了処理
			if(server.pCount <= 2){
				server.broadcastStr("END");
				//残ったコネクションをすべて閉じる
				for(Iterator<Connection> it = server.connect.iterator(); it.hasNext();){
					Connection c = it.next();
					c.close();
					server.connect.remove(c);
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

		scanner.close();
	}
}
