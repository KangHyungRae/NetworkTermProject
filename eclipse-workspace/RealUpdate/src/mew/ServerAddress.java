package mew;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ServerAddress extends JFrame {

	public JButton confirmBtn;
	public JTextField ipText;
	private LoginUI loginUI;
	
	//서버 실행 후 서버 아이피 주소 입력UI
	public ServerAddress(LoginUI loginUI) {
		this.loginUI = loginUI;
		initialize();
	}
	
	//서버 아이피 주소 입력UI 세팅
	private void initialize() {
		setTitle("\uC11C\uBC84 \uC544\uC774\uD53C \uC8FC\uC18C \uC785\uB825");	//"서버 아이피 주소 입력" title
		setBounds(100, 100, 306, 95);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 10, 266, 37);
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		//IP 주소 입력칸
		ipText = new JTextField();
		ipText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					loginUI.ipBtn.setText(ipText.getText());
					loginUI.setVisible(true);
					dispose();
					loginUI.idText.requestFocus();
				}
			}
		});
		ipText.setText("127.0.0.1");
		panel.add(ipText, BorderLayout.CENTER);
		ipText.setColumns(10);
		
		//"확인" 버튼
		confirmBtn = new JButton("\uD655\uC778");	//"확인"
		confirmBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				loginUI.ipBtn.setText(ipText.getText());
				loginUI.setVisible(true);
				dispose();
				loginUI.idText.requestFocus();
			}
		});
		panel.add(confirmBtn, BorderLayout.EAST);
		setVisible(true);
	}

}
