import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import java.awt.Container;
import java.awt.BorderLayout;

class UserGui extends JFrame{
  public static void main(String args[]){
    UserGui frame = new UserGui("少数決ゲーム");
    frame.setVisible(true);
  }

  UserGui(String title){	//コンストラクタ
    setTitle(title);
    setBounds(100, 100, 300, 400);
		//閉じたときの動作
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel p1 = new JPanel();

		//ウェルカムメッセージ
    JLabel label1 = new JLabel();
		label1.setText("<html>Welcome!<br>Tell me your name!<br>");

		JPanel p2 = new JPanel();
		//Name入力エリア
		JLabel label2 = new JLabel("Name",SwingConstants.LEFT);
		JTextField text1 = new JTextField(10);


    p1.add(label1);
		p2.add(label2);
		p2.add(text1);

    Container contentPane = getContentPane();
    contentPane.add(p1, BorderLayout.NORTH);
    contentPane.add(p2, BorderLayout.CENTER);
  }
}
