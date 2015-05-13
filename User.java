import java.util.scanner;

public class User{
	int money;
    int bet = 0;
    Connection connect;
    Scanner scanner = new Scanner(System.in);

    boolean selectChoice(){
        boolean choice;
        String input;

        System.out.println("Aなら1,　Bなら0を入力　>");
        input = scanner.next();
        choice = Integer.parseInt(input) ? true : false;

        while(true){
            System.out.println("掛け金を入力　>");
            bet = scanner.next();
            if(bet < money){
                return choice;
            }
            System.out.println("掛け金は所持金以下にしてください");
        }
	}

    boolean sendChoice(boolean choice){
        /*
        connect.send(choice, bet);
        みたいに書けるといいかも
        */
    }

    boolean receiveResult(){
        String result;
        boolean isWon;
        /*
        result = connect.receive();
        こんな感じで受け取りたい
        */

        /*
        受け取る文字列のフォーマットは,
        "(w | w以外)(掛け金の文字列表現)"
        と勝手に決めて処理してる
        */
        isWon = (result.charAt(0) == 'w') ? true : false;
        if(isWon){
            updateMoney(result.substring(1, result.length());
        }
    }

    int updateMoney(int bet){
        money += bet;
    }


}
