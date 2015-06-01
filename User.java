import java.util.Scanner;

//プレイヤーを表すクラス
public class User{
    private String name;
    private int money;
    private int bet = 0;
    private boolean choice;
    private Connection connect;
    Scanner scanner;

    public User(String name, int money, String host_name){
        this.name = name;
        this.money = money;
        connect = new Connection(host_name);
        scanner = new Scanner(System.in);
    }


    public String getName(){
        return name;
    }

    public int getMoney(){
        return money;
    }

    public String receiveQuestion(){
        String question = null;
        connect.receiveStr( question );

        return question;
    }

    //選択と掛け金を入力
    public void inputChoice(){
        boolean choice;
        String input;

        System.out.println("Aなら1,　Bなら0を入力　>");
        input = scanner.next();
        choice = Integer.parseInt(input) == 1 ? true : false;

        while(true){
            System.out.println("掛け金を入力　>");
            bet = Integer.parseInt(scanner.next());

            if(bet > money){        //所持金以下を指定するまで繰り返す
                System.out.println("掛け金は所持金以下にしてください");
            }

            this.choice = choice;
            return;
        }
    }

    //選択をサーバーに送信
    public void sendChoice(){
        connect.sendChoice(choice, bet);
    }

    //サーバーから結果を受信して勝っていたらtrueを返す。
    public boolean receiveResult(){
        boolean result = false; //Aが少数派だったらtrueがくる
        int bet = 0;

        //結果の受け取り
        connect.receiveChoice(result, bet);

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

    public static void main(){
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

        //メインループ
        while( user.getMoney() > 0 ){
            System.out.println( user.receiveQuestion() );
            user.inputChoice();
            user.sendChoice();
            isWon = user.receiveResult();

            if( isWon ){
                System.out.println("you are Minority.");
                user.updateMoney();
                System.out.print("you got $" + user.bet + ".");
            }else{
                System.out.println("you are Majority.");
            }
            System.out.println("remaining money : $" + user.money);
        }

        if(user.money > 0){
            System.out.println("you are survived. you got $" + user.money + ".");
        }else{
            System.out.println("Game Over...");
        }

        scanner.close();
        user.endGame();
    }
}
