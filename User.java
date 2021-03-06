import java.util.Scanner;

//プレイヤーを表すクラス
public class User{
    public String name;             //プレイヤー名
    public int money;               //所持金
    public int bet = 0;             //掛け金。結果の受け取りの際には獲得金額
    public boolean choice;          //yesを選んだらtrue
    public Connection connect;      //汎用ソケット
    public Connection chatlistener; //チャット用の受信ソケット
    public Scanner scanner;
    static final byte WON  = 1 << 0;
    static final byte DRAW = 1 << 1;

    public User(String name, int money, String host_name){
        this.name = name;
        this.money = money;
        connect = new Connection(host_name, name, this);
        scanner = new Scanner(System.in);

        //サーバーから文字列を受け取ったらチャット用Connectionを作る
        connect.receiveStr();
        chatlistener = new Connection(host_name, name, this);

        //チャット用のソケットは常に開いておく
        chatlistener.setMode("listener");
        chatlistener.start();
    }

    //ユーザー名のgetter
    public String getName(){
        return name;
    }

    //所持金のgetter
    public int getMoney(){
        return money;
    }


    //選択と掛け金を入力する。戻り値は入力内容を送信用のフォーマットに直した文字列。
    public String inputChoice(){
        boolean choice;
        String input;

        while(true){
            System.out.print("input \"yes\" or \"no\" > ");
            input = scanner.next();
            if( !input.equals("yes") && !input.equals("no") ) continue;
            choice = input.equals("yes") ? true : false;
            break;
        }

        while(true){
            System.out.print("input your bet > ");

            try {
                bet = Integer.parseInt(scanner.next());
            } catch (NumberFormatException e) {
                System.out.println("input number.");
                continue;
            }

            //所持金以下を指定するまで繰り返す
            if(bet > money){
                System.out.println("you can't bet money more than you have.");
                continue;
            }
            //最低でも200は賭けないとだめ
            if(bet < 200 && money >= 200){
                System.out.println("you must bet at least $200.");
                continue;
            }

            this.choice = choice;
            money -= bet;

            return input + "\n" + bet;
        }
    }

    //サーバーから結果を受信して判定結果を返す。
    public byte receiveResult(){
        byte result = 0;
        String judgeStr;

        //結果の受け取り
        judgeStr = connect.receiveStr();

        switch (judgeStr) {
        case "yes":
            result |= choice ? WON : 0;
            bet = Integer.parseInt( connect.receiveStr() );
            break;

        case "same":
            result |= DRAW;
            Integer.parseInt( connect.receiveStr() );   //読み飛ばしておく
            break;

        case "no":
            result |= !choice ? WON : 0;
            bet = Integer.parseInt( connect.receiveStr() );
            break;

        default:
            break;
        }

        return result;
    }

    //金額の更新
    public void updateMoney(){
        money += this.bet;
    }

    //ゲームの終了処理
    public void endGame(){
        chatlistener.close();
        connect.close();
        scanner.close();
    }

    public static void main(String args[]){
        User user;
        Scanner scanner;
        byte result;

        //開始処理
        System.out.println("welcome!");
        scanner = new Scanner(System.in);

        System.out.print("tell me your name : ");
        String name = scanner.next();

        System.out.print("input host server  : ");
        String host_name = scanner.next();

        user = new User(name, 1000, host_name);





        System.out.println("you have $"+ user.money +".");

        //メインループ
        while( true ){
            //問題の受け取り
            System.out.println( user.connect.receiveStr() );

            //チャット開始
            Connection.isChatMode = true;
            user.connect.setMode("discuss");
            user.connect.start();

            try {
                user.connect.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //投票処理
            user.connect = new Connection(user.connect);
            user.connect.setMode("vote");
            user.connect.start();
            try {
                user.connect.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            result = user.receiveResult();

            //勝敗の反映
            if( result == WON ){
                System.out.println("you are Minority.");
                user.updateMoney();
                System.out.println("you got $" + user.bet + ".");
            }else if( result == DRAW){
                System.out.println("draw.");
                user.updateMoney();
                System.out.println("your bet returned.");
            }else{
                System.out.println("you are Majority.");
            }

            System.out.println("remaining money : $" + user.money);

            //他のプレイヤーの状態取得
            user.connect = new Connection(user.connect);
            user.connect.setMode("interim");
            user.connect.start();

            try {
                user.connect.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //途中敗退の処理
            if(user.money <= 0){
                System.out.println("Game Over...");
                try {
                    user.chatlistener.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }

            //サーバーからENDが送られてきたら終了処理を開始
            String msg = user.connect.receiveStr();

            if( msg.equals("END") ){
                System.out.println("you are survived. you got $" + user.money + ".");
                break;
            }

            System.out.println(msg);

            user.connect = new Connection(user.connect);
        }


        scanner.close();
        user.endGame();
    }
}
