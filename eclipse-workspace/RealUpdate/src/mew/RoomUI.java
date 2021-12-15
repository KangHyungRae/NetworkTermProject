package mew;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class RoomUI extends JFrame {

	private SixClient client;
	private Room room;

	public JTextArea chatArea;
	public JTextField chatField;
	public JList uList;
	public JLabel MyCard1;
	public JLabel MyCard2;
	public JLabel EnCard1;
	public JLabel EnCard2;
	public JPanel MyCard;
	public JPanel EnCard;
	public JLabel lb_win;
	public JLabel lb_lose;
	public DefaultListModel model;

	public RoomUI(SixClient client, Room room) {
		this.client = client;
		this.room = room;
		setTitle("게임방 : " + room.toProtocol());
		initialize();
	}

	private void initialize() {
		setBounds(100, 100, 777, 538);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 204, 525, 188);
		panel.setBorder(new TitledBorder(null, "\uCC44\uD305",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);

		chatArea = new JTextArea();
		scrollPane.setViewportView(chatArea);
		chatArea.setBackground(new Color(224, 255, 255));
		chatArea.setEditable(false);
		chatArea.append("채팅 시작\n");

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(12, 402, 525, 34);
		panel_1.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		getContentPane().add(panel_1);
		panel_1.setLayout(null);

		chatField = new JTextField();
		chatField.setBounds(0, 0, 523, 30);
		panel_1.add(chatField);
		chatField.setColumns(10);
		chatField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				}
			}

		});
		chatField.requestFocus();

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(549, 10, 187, 184);
		panel_2.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "\uCC38\uC5EC\uC790",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_2.add(scrollPane_1, BorderLayout.CENTER);

		uList = new JList(new DefaultListModel());
		model = (DefaultListModel) uList.getModel();
		scrollPane_1.setViewportView(uList);

		JButton roomSendBtn = new JButton("\uBCF4\uB0B4\uAE30");
		roomSendBtn.setBounds(548, 402, 91, 34);
		roomSendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				msgSummit();
				chatField.requestFocus();
			}
		});
		getContentPane().add(roomSendBtn);

		JButton RoomGamesend = new JButton("게임시작");
		RoomGamesend.setBounds(651, 402, 85, 34);
		RoomGamesend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				GameSummit();
			}
		});
		getContentPane().add(RoomGamesend);

		MyCard = new JPanel();
		MyCard.setBounds(12, 10, 259, 184);
		MyCard.setBorder(new TitledBorder(null, "나의 패",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		getContentPane().add(MyCard);
		MyCard.setLayout(null);

		MyCard1 = new JLabel();
		MyCard1.setBounds(12, 24, 108, 150);
		MyCard.add(MyCard1);

		MyCard2 = new JLabel();
		MyCard2.setBounds(139, 24, 108, 150);
		MyCard.add(MyCard2);

		EnCard = new JPanel();
		EnCard.setLayout(null);
		EnCard.setBorder(new TitledBorder(null, "적의 패",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		EnCard.setBounds(277, 10, 259, 184);
		getContentPane().add(EnCard);

		EnCard1 = new JLabel();
		EnCard1.setBounds(12, 24, 108, 150);
		EnCard.add(EnCard1);

		EnCard2 = new JLabel();
		EnCard2.setBounds(139, 24, 108, 150);
		EnCard.add(EnCard2);

		JButton OtherInfo = new JButton("상대전적");
		OtherInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				OtherInfo();
			}
		});
		OtherInfo.setBounds(549, 358, 187, 34);
		getContentPane().add(OtherInfo);

		JPanel info = new JPanel();
		info.setLayout(null);
		info.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "상대전적",	//"내정보" title
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		info.setBounds(549, 204, 187, 147);
		getContentPane().add(info);

		JLabel Win = new JLabel("승리");
		Win.setHorizontalAlignment(SwingConstants.CENTER);
		Win.setBounds(0, 55, 57, 15);
		info.add(Win);

		JLabel lose = new JLabel("패배");
		lose.setHorizontalAlignment(SwingConstants.CENTER);
		lose.setBounds(0, 90, 57, 15);
		info.add(lose);

		lb_win = new JLabel("-");
		lb_win.setBounds(57, 55, 102, 15);
		info.add(lb_win);

		lb_lose = new JLabel("-");
		lb_lose.setBounds(57, 90, 102, 15);
		info.add(lb_lose);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					client.getUser()
							.getDos()
							.writeUTF(
									User.GETOUT_ROOM + "/" + room.getRoomNum());
					for (int i = 0; i < client.getUser().getRoomArray().size(); i++) {
						if (client.getUser().getRoomArray().get(i).getRoomNum() == room
								.getRoomNum()) {
							client.getUser().getRoomArray().remove(i);
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void msgSummit() {
		String string = chatField.getText();
		if (!string.equals("")) {
			try {
				// 채팅방에 메시지 보냄
				client.getDos().writeUTF(
						User.ECHO02 + "/" + room.getRoomNum() + "/"
								+ client.getUser().toString() + string);
				chatField.setText("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void OtherInfo() {
		try {
			// 채팅방에 메시지 보냄
			client.getDos().writeUTF(
					User.GETINFO + "/" + room.getRoomNum());
			chatField.setText("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void GameSummit() {
		try {
			// 채팅방에 메시지 보냄
			client.getDos().writeUTF(
					User.GAMESTART + "/" + room.getRoomNum());
			client.getDos().writeUTF(
					User.GETINFO + "/" + room.getRoomNum());
			chatField.setText("");
			chatField.setText("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void updateMyCardImage( int cardNum1, int cardNum2) {
		String imgURL1 = "img_card_" + cardNum1 + ".png";
		String imgURL2 = "img_card_" + cardNum2 + ".png";
		Image image1 = new ImageIcon(imgURL1).getImage();
		Image image2 = new ImageIcon(imgURL2).getImage();


		MyCard1.setIcon(new ImageIcon(image1.getScaledInstance(106,148,image1.SCALE_SMOOTH)));
		MyCard1.setBounds(12, 24, 108, 150);
		MyCard1.revalidate();

		MyCard2.setIcon(new ImageIcon(image2.getScaledInstance(106,148,image2.SCALE_SMOOTH)));
		MyCard2.setBounds(139, 24, 108, 150);
		MyCard2.revalidate();
		MyCard.add(MyCard2);

		MyCard.setVisible(true);
		repaint();
	}
	public void NotifyWin(String msg) {
		JOptionPane.showMessageDialog(null,msg+"에의해 승리하였습니다");
	}

	public void NotifyLose(String msg) {
		JOptionPane.showMessageDialog(null,msg+"에의해 패배하였습니다");
	}
	public void updateOpCardImage(int cardNum1, int cardNum2) {
		String imgURL1 = "img_card_" + cardNum1 + ".png";
		String imgURL2 = "img_card_" + cardNum2 + ".png";
		Image image3 = new ImageIcon(imgURL1).getImage();
		Image image4 = new ImageIcon(imgURL2).getImage();

		EnCard1.setIcon(new ImageIcon(image3.getScaledInstance(106,148,image3.SCALE_SMOOTH)));
		EnCard1.setBounds(12, 24, 108, 150);
		EnCard1.revalidate();

		EnCard2.setIcon(new ImageIcon(image4.getScaledInstance(106,148,image4.SCALE_SMOOTH)));
		EnCard2.setBounds(139, 24, 108, 150);
		EnCard2.revalidate();
		EnCard.add(EnCard2);

		EnCard.setVisible(true);
		repaint();
	}

}

