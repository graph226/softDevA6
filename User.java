import java.util.scanner;

public class User{
	int money;

    boolean send(){
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Aなら1,　Bなら0を入力　>");
        input = scanner.next();
        return Integer.parseInt(input) ? true : false;
	}
}
