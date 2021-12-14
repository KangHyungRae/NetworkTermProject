package mew;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class RestRoomUI extends JFrame implements ActionListener {

	public int lastRoomNum = 100;
	public JButton makeRoomBtn, getInRoomBtn, whisperBtn, sendBtn;
	public JTree userTree;
	public JList roomList;
	public JTextField chatField;
	public JTextArea restRoomArea;
	public JLabel lb_id, lb_nick;
	public JTextField lb_ip;

	private SixClient client;
	public ArrayList<User> userArray; // 사용자 목록 배열
	public String currentSelectedTreeNode;
	public DefaultListModel model;
	public DefaultMutableTreeNode level_1;
	public DefaultMutableTreeNode level_2_1;
	public DefaultMutableTreeNode level_2_2;
	
	//대기실UI
	public RestRoomUI(SixClient sixClient) {
		setTitle("대기실");
		userArray = new ArrayList<User>();
		client = sixClient;
		initialize();
	}
	//대기실UI 세팅
	private void initialize() {
		setBounds(100, 100, 700, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		//Title 밑에 menu bar "일반" 메뉴
		JMenu basicMenus = new JMenu("\uC77C\uBC18");
		basicMenus.addActionListener(this);
		menuBar.add(basicMenus);

		//"일반" 메뉴 클릭하면 "끝내기" 
		JMenuItem exitItem = new JMenuItem("\uB05D\uB0B4\uAE30");
		exitItem.addActionListener(this);
		basicMenus.add(exitItem);

		getContentPane().setLayout(null);

		//대기실에 "채팅방" 부분
		JPanel room = new JPanel();
		room.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "\uCC44\uD305\uBC29",	//"채팅방" title
				TitledBorder.CENTER, TitledBorder.TOP, null, null));
		room.setBounds(12, 10, 477, 215);
		getContentPane().add(room);
		room.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		room.add(scrollPane, BorderLayout.CENTER);

		// 리스트 객체와 모델 생성
		roomList = new JList(new DefaultListModel());
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// 채팅방 목록 중 하나를 선택한 경우,
				// 선택한 방의 방번호를 전송
				String temp = (String) roomList.getSelectedValue();

				try {
					client.getUser()
							.getDos()
							.writeUTF(
									User.UPDATE_SELECTEDROOM_USERLIST + "/"
											+ temp.substring(0, 3));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		model = (DefaultListModel) roomList.getModel();
		roomList.setBackground(new Color(224, 255, 255));
		scrollPane.setViewportView(roomList);

		JPanel panel_2 = new JPanel();
		room.add(panel_2, BorderLayout.SOUTH);

		//대기실에 방 만들기 버튼
		makeRoomBtn = new JButton("\uBC29 \uB9CC\uB4E4\uAE30");
		makeRoomBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		makeRoomBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// 방만들기 버튼 클릭
				createRoom();
			}
		});
		panel_2.setLayout(new GridLayout(0, 2, 0, 0));
		panel_2.add(makeRoomBtn);
		
		//대기실에 방 들어가기 버튼
		getInRoomBtn = new JButton("\uBC29 \uB4E4\uC5B4\uAC00\uAE30");
		getInRoomBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// 방 들어가기
				getIn();
			}
		});
		panel_2.add(getInRoomBtn);

		//대기실에 "사용자 목록" 부분
		JPanel user = new JPanel();
		user.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"),
				"\uC0AC\uC6A9\uC790 \uBAA9\uB85D", TitledBorder.CENTER,	//"사용자 목록" title
				TitledBorder.TOP, null, null));
		user.setBounds(501, 10, 171, 215);
		getContentPane().add(user);
		user.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		user.add(scrollPane_1, BorderLayout.CENTER);

		// 사용자목록, 트리구조
		userTree = new JTree();
		userTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				currentSelectedTreeNode = arg0.getPath().getLastPathComponent()
						.toString();
			}
		});
		level_1 = new DefaultMutableTreeNode("참여자");
		level_2_1 = new DefaultMutableTreeNode("채팅방");
		level_2_2 = new DefaultMutableTreeNode("대기실");
		level_1.add(level_2_1);
		level_1.add(level_2_2);

		DefaultTreeModel model = new DefaultTreeModel(level_1);
		userTree.setModel(model);

		scrollPane_1.setViewportView(userTree);

		JPanel panel_1 = new JPanel();
		user.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));

		//대기실에 "귓속말" 버튼
		whisperBtn = new JButton("\uADD3\uC18D\uB9D0");
		whisperBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				StringTokenizer token = new StringTokenizer(
						currentSelectedTreeNode, "("); // 토큰 생성
				String temp = token.nextToken(); // 토큰으로 분리된 스트링
				temp = token.nextToken();

				// 닉네임 제외하고 아이디만 따옴
				chatField.setText("/" + temp.substring(0, temp.length() - 1)
						+ " ");
				chatField.requestFocus();
			}
		});
		panel_1.add(whisperBtn);

		//대기실에 "대 기 실" 부분
		JPanel restroom = new JPanel();
		restroom.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "\uB300 \uAE30 \uC2E4",	//"대 기 실" title
				TitledBorder.CENTER, TitledBorder.TOP, null, Color.DARK_GRAY));
		restroom.setBounds(12, 235, 477, 185);
		getContentPane().add(restroom);
		restroom.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		restroom.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JScrollPane scrollPane_4 = new JScrollPane();
		panel.add(scrollPane_4);

		chatField = new JTextField();

		chatField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					msgSummit();
				}
			}

		});
		scrollPane_4.setViewportView(chatField);
		chatField.setColumns(10);

		//"보내기" 버튼
		sendBtn = new JButton("\uBCF4\uB0B4\uAE30");
		sendBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				msgSummit();
				chatField.requestFocus();
			}
		});
		panel.add(sendBtn);

		JScrollPane scrollPane_2 = new JScrollPane();
		restroom.add(scrollPane_2, BorderLayout.CENTER);

		restRoomArea = new JTextArea();
		restRoomArea.setBackground(new Color(224, 255, 255));
		restRoomArea.setEditable(false);
		scrollPane_2.setViewportView(restRoomArea);

		//"내정보" 부분
		JPanel info = new JPanel();
		info.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "\uB0B4 \uC815\uBCF4",	//"내정보" title
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		info.setBounds(501, 235, 171, 185);
		getContentPane().add(info);
		info.setLayout(null);

		//"내정보" 의 IP
		JLabel lblNewLabel = new JLabel("IP");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(-25, 54, 57, 15);
		info.add(lblNewLabel);
		//"내정보" 의 아이디
		JLabel lblNewLabel_2 = new JLabel("\uC544\uC774\uB514");	//아이디
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(0, 89, 57, 15);
		info.add(lblNewLabel_2);
		//"내정보" 의 닉네임
		JLabel lblNewLabel_3 = new JLabel("\uB2C9\uB124\uC784");	//닉네임
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setBounds(0, 129, 57, 15);
		info.add(lblNewLabel_3);

		lb_id = new JLabel("-");
		lb_id.setBounds(57, 89, 102, 15);
		info.add(lb_id);

		lb_nick = new JLabel("-");
		lb_nick.setBounds(57, 129, 102, 15);
		info.add(lb_nick);

		lb_ip = new JTextField();
		lb_ip.setBounds(57, 51, 102, 21);
		lb_ip.setEditable(false);
		info.add(lb_ip);
		lb_ip.setColumns(10);
		chatField.requestFocus();
		setVisible(true);
		chatField.requestFocus();

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			// 메뉴1 파일 메뉴
			case "끝내기":
				exit();
				break;
		}
	}

	private void msgSummit() {
		// 메시지전송
		String string = chatField.getText();

		if (!string.equals("")) {
			if (string.substring(0, 1).equals("/")) {
				StringTokenizer token = new StringTokenizer(string, " "); // 토큰
				// 생성
				String id = token.nextToken(); // 토큰으로 분리된 스트링
				String msg = token.nextToken();

				try {
					client.getDos().writeUTF(User.WHISPER + id + "/" + msg);
					restRoomArea.append(id + "님에게 귓속말 : " + msg + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				chatField.setText("");
			} else {

				try {
					// 대기실에 메시지 보냄
					client.getDos().writeUTF(User.ECHO01 + "/" + string);
				} catch (IOException e) {
					e.printStackTrace();
				}
				chatField.setText("");
			}
		}
	}
	//로그아웃
	private void exit() {
		try {
			client.getUser().getDos().writeUTF(User.LOGOUT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//게임방 만들기
	private void createRoom() {
		String roomname = "게임방";
		Room newRoom = new Room(roomname); // 방 객체 생성
		newRoom.setRoomNum(lastRoomNum);
		newRoom.setrUI(new RoomUI(client, newRoom)); // UI

		// 클라이언트가 접속한 방 목록에 추가
		client.getUser().getRoomArray().add(newRoom);

		try {
			client.getDos().writeUTF(
					User.CREATE_ROOM + "/" + newRoom.toProtocol());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//게임방 들어가기
	private void getIn() {
		// 선택한 방 정보
		String selectedRoom = (String) roomList.getSelectedValue();
		StringTokenizer token = new StringTokenizer(selectedRoom, "/"); // 토큰 생성
		String rNum = token.nextToken();
		String rName = token.nextToken();

		Room theRoom = new Room(rName); // 방 객체 생성
		theRoom.setRoomNum(Integer.parseInt(rNum)); // 방번호 설정
		theRoom.setrUI(new RoomUI(client, theRoom)); // UI

		// 클라이언트가 접속한 방 목록에 추가
		client.getUser().getRoomArray().add(theRoom);

		try {
			client.getDos().writeUTF(
					User.GETIN_ROOM + "/" + theRoom.getRoomNum());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}