import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class UserGui extends JFrame {
  public static void main(String args[]){
    UserGui frame = new UserGui("少数決ゲーム");
    frame.setVisible(true);
  }

  UserGui(String title){	//コンストラクタ
		//User user;
    setTitle(title);
    setBounds(100, 100, 300, 200);
		//閉じたときの動作
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel p1 = new JPanel();

		//ウェルカムメッセージ
    JLabel label1 = new JLabel();
		label1.setText("<html>Welcome!<br>Inout your name and server!<br>");

		//Name入力エリア
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2,2,0,30));
		JLabel label2 = new JLabel("Name",SwingConstants.LEFT);
		final JTextField text1 = new JTextField(10);

		//Host入力エリア
		JLabel label3 = new JLabel("Host Server",SwingConstants.LEFT);
		final JTextField text2 = new JTextField(10);

		JPanel p3 = new JPanel();
		JButton button1 = new JButton("入力");
		button1.addActionListener(
			new ActionListener(){
				String name;		//宣言の仕方を変更
				String host_name;
				User user;
				public void actionPerformed(ActionEvent event){
					name = text1.getText();
					host_name = text2.getText();
					user = new User(name, 1000, host_name);
				}
			}
		);






    p1.add(label1);
		p2.add(label2);
		p2.add(text1);
		p2.add(label3);
		p2.add(text2);
		p3.add(button1);

    Container contentPane = getContentPane();
    contentPane.add(p1, BorderLayout.NORTH);
    contentPane.add(p2, BorderLayout.CENTER);
    contentPane.add(p3, BorderLayout.PAGE_END);
  }
}
