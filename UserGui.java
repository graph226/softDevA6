import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import java.awt.Container;
import java.awt.BorderLayout;

class UserGui extends JFrame{
  public static void main(String args[]){
    UserGui frame = new UserGui("少数欠ゲーム");
    frame.setVisible(true);
  }

  UserGui(String title){	//コンストラクタ
    setTitle(title);
    setBounds(100, 100, 300, 250);
		//閉じたときの動作
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel p = new JPanel();

    JLabel label1 = new JLabel();
		label1.setText("<html>Welcome!<br>Tell me your name!");

    p.add(label1);

    Container contentPane = getContentPane();
    contentPane.add(p, BorderLayout.CENTER);
  }
}