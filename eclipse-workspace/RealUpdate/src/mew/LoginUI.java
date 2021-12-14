package mew;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.event.*;
import java.io.IOException;

public class LoginUI extends JFrame {

	public boolean confirm = false;
	public JTextField idText;
	public JTextField pwText;
	public JButton loginBtn, signUpBtn;
	public MemberUI mem;
	public JButton ipBtn;
	private SixClient client;

	//로그인UI
	public LoginUI(SixClient sixClient) {
		setTitle("\uB85C\uADF8\uC778");	//title = 로그인
		ServerAddress sd = new ServerAddress(this);
		this.client = sixClient;
		loginUIInitialize();
	}

	//로그인UI설정
	private void loginUIInitialize() {
		setBounds(100, 100, 335, 218);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(12, 10, 295, 160);
		getContentPane().add(panel);
		panel.setLayout(null);

		//로그인UI "아이디"
		JLabel lblNewLabel = new JLabel("\uC544\uC774\uB514");	//아이디
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(60, 55, 57, 15);
		panel.add(lblNewLabel);
		
		//로그인UI "비밀번호"
		JLabel lblNewLabel_1 = new JLabel("\uBE44\uBC00\uBC88\uD638");	//비밀번호
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(60, 86, 57, 15);
		panel.add(lblNewLabel_1);
		//아이디 입력칸
		idText = new JTextField();
		idText.setBounds(129, 52, 116, 21);
		panel.add(idText);
		idText.setColumns(10);
		//비밀번호 입력칸
		pwText = new JTextField();
		pwText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				}
			}

		});
		pwText.setBounds(129, 83, 116, 21);
		panel.add(pwText);
		pwText.setColumns(10);
		//로그인UI 로그인 버튼
		loginBtn = new JButton("\uB85C\uADF8\uC778");	//로그인
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		loginBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				msgSummit();
			}
		});
		loginBtn.setBounds(50, 111, 97, 23);
		panel.add(loginBtn);
		//로그인UI 회원가입 버튼
		signUpBtn = new JButton("\uD68C\uC6D0\uAC00\uC785");	//회원가입
		signUpBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		signUpBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 회원가입
				mem = new MemberUI(client);
			}
		});
		signUpBtn.setBounds(149, 111, 97, 23);
		panel.add(signUpBtn);
		
		//로그인UI "서버 아이피"
		JLabel lblNewLabel_2 = new JLabel("\uC11C\uBC84\uC544\uC774\uD53C");	//서버 아이피
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(12, 10, 78, 15);
		panel.add(lblNewLabel_2);
		
		//아이피 주소가 나와있는 버튼
		ipBtn = new JButton("\uC544\uC774\uD53C \uC785\uB825");	//로그인 한 IP 주소
		ipBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ServerAddress sd = new ServerAddress(LoginUI.this);
				setVisible(false);
			}
		});
		ipBtn.setBounds(93, 6, 97, 23);
		panel.add(ipBtn);
	}

	private void msgSummit() {
		new Thread(new Runnable() {
			public void run() {

				// 소켓생성
				if (client.serverAccess()) {
					try {
						// 로그인정보(아이디+패스워드) 전송
						client.getDos().writeUTF(
								User.LOGIN + "/" + idText.getText() + "/"
										+ pwText.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();
	}
}
