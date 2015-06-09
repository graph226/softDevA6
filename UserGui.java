import java.awt.*;
import java.awt.event.*;

class PrefFrame extends Frame {

	public PrefFrame(String title) {
		//Frame's title
		setTitle(title);

		//When closing window...
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}
}

public class UserGui {

	public static void main(String args[]){
		PrefFrame frm = new PrefFrame("少数欠ゲーム");
		frm.setLocation(300, 200);
		frm.setSize(250, 350);
		frm.setBackground(Color.LIGHT_GRAY);
		frm.setVisible(true);
	}
}

