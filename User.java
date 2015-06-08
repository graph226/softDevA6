import java.util.Scanner;

//プレイヤーを表すクラス
public class User{
    private String name;    //プレイヤー名
    private int money;      //所持金
    private int bet = 0;    //掛け金。結果の受け取りの際には獲得金額
    private boolean choice; //Aを選んだらtrue
    private Connection connect;
    Scanner scanner;

    public User(String name, int money, String host_name){
        this.name = name;
        this.money = money;
        connect = new Connection(host_name, name, this);
        scanner = new Scanner(System.in);
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

        System.out.println("input \"yes\" or \"no\" > ");
        input = scanner.next();
        choice = input.equals("yes") ? true : false;

        while(true){
            System.out.println("input your bet > ");
            bet = Integer.parseInt(scanner.next());

          //所持金以下を指定するまで繰り返す
            if(bet > money){
                System.out.println("you can't bet money more than you have.");
                continue;
            }

            this.choice = choice;
            money -= bet;

            return input + "\n" + bet;
        }
    }

    //サーバーから結果を受信して勝っていたらtrueを返す。
    public boolean receiveResult(){
        boolean result = false; //Aが少数派だったらtrueがくる

        //結果の受け取り
        result = connect.receiveStr().equals("yes");
        bet = Integer.parseInt( connect.receiveStr() );

        return result == choice;
    }

    //金額の更新
    public void updateMoney(){
        money += this.bet;
    }

    //ゲームの終了処理
    public void endGame(){
        connect.close();
        scanner.close();
    }

    public static void main(String args[]){
        User user;
        Scanner scanner;
        boolean isWon;

        //開始処理
        System.out.println("welcome!");
        scanner = new Scanner(System.in);

        System.out.println("tell me your name : ");
        String name = scanner.next();

        System.out.println("input host server  : ");
        String host_name = scanner.next();

        user = new User(name, 1000, host_name);

        System.out.println("you have $"+ user.money +".");

        //メインループ
        while( true ){
            //問題の受け取り
            System.out.println( user.connect.receiveStr() );

            //投票処理
            user.connect.setMode("vote");
            user.connect.start();
            try {
                user.connect.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            isWon = user.receiveResult();

            //勝敗の反映
            if( isWon ){
                System.out.println("you are Minority.");
                user.updateMoney();
                System.out.print("you got $" + user.bet + ".");
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

            //サーバーからENDが送られてきたら終了処理を開始
            String msg = user.connect.receiveStr();

            if( msg.equals("END") ){
                if(user.money <= 0){
                    System.out.println("Game Over...");
                }else{
                    System.out.println("you are survived. you got $" + user.money + ".");
                }
                break;
            }

            System.out.println(msg);

            user.connect = new Connection(user.connect);
        }

        scanner.close();
        user.endGame();
    }
}
