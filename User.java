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
    }

    int updateMoney(int bet){
        money += bet;
    }


}
