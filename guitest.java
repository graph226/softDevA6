import java.awt.*;

public class guitest extends Frame {

	public guitest () {
		super();
		setTitle("Hello");
		setSize(300,150);

		Label mylabel;
		mylabel = new Label ("hogehoge");
		this.add ("Center", mylabel);

		Button mybutton;
		mybutton = new Button ("OK");
		this.add ("South",mybutton);
	}

	public static void main(String args []){
		new guitest ().setVisible(true);
	}
}
