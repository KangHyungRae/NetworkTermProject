package mew;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ServerThread implements Runnable {

	private ArrayList<User> userArray; // 서버에 접속한 사용자들
	private ArrayList<Room> roomArray; // 서버가 열어놓은 채팅방들
	private User user; // 현재 스레드와 연결된(소켓이 생성된) 사용자
	private JTextArea jta;
	private int room_num;
	private boolean onLine = true;

	int [][] jokbo= new int[21][21];

	private DataOutputStream thisUser;

	DataBase DB=new DataBase();//DB서버 연동

	ServerThread(JTextArea jta, User person, ArrayList<User> userArray,
				 ArrayList<Room> roomArray) {
		this.roomArray = roomArray;
		this.userArray = userArray;
		this.userArray.add(person); // 배열에 사용자 추가
		this.user = person;
		this.jta = jta;
		this.thisUser = person.getDos();
	}

	@Override
	public void run() {
		DataInputStream dis = user.getDis(); // 입력 스트림 얻기

		while (onLine) {
			try {
				String receivedMsg = dis.readUTF(); // 메시지 받기(대기)
				dataParsing(receivedMsg); // 메시지 해석
				jta.append("성공 : 메시지 읽음 -" + receivedMsg + "\n");
				jta.setCaretPosition(jta.getText().length());
			} catch (IOException e) {
				try {
					user.getDis().close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					jta.append("에러 : 서버스레드-읽기 실패\n");
					break;
				}
			}
		}
	}

	// 데이터를 구분
	public synchronized void dataParsing(String data) {
		StringTokenizer token = new StringTokenizer(data, "/"); // 토큰 생성
		String protocol = token.nextToken(); // 토큰으로 분리된 스트링을 숫자로
		String id, pw, rNum, nick, rName, msg, nName, name, email;
		System.out.println("서버가 받은 데이터 : " + data);

		switch (protocol) {
			case User.LOGIN: // 로그인
				// 사용자가 입력한(전송한) 아이디와 패스워드
				id = token.nextToken();
				pw = token.nextToken();
				login(id, pw);
				break;
			case User.LOGOUT: // 로그아웃
				logout();
				break;
			case User.MEMBERSHIP: // 회원가입
				name = token.nextToken();
				nName = token.nextToken();
				id = token.nextToken();
				pw = token.nextToken();
				email = token.nextToken();
				member(name, nName, id, pw, email);
				break;
			case User.UPDATE_USERLIST: // 대기실 사용자 목록
				userList(thisUser);
				break;
			case User.UPDATE_ROOM_USERLIST: // 채팅방 사용자 목록
				// 방번호읽기
				rNum = token.nextToken();
				userList(rNum, thisUser);
				break;
			case User.UPDATE_SELECTEDROOM_USERLIST: // 대기실에서 선택한 채팅방의 사용자 목록
				// 방번호읽기
				rNum = token.nextToken();
				selectedRoomUserList(rNum, thisUser);
				break;

			case User.UPDATE_ROOMLIST: // 방 목록
				roomList(thisUser);
				break;
			case User.CREATE_ROOM: // 방만들기
				rNum = token.nextToken();
				rName = token.nextToken();
				createRoom(rNum, rName);
				break;
			case User.GETIN_ROOM: // 방 들어가기
				rNum = token.nextToken();
				getInRoom(rNum);
				break;
			case User.GETOUT_ROOM: // 방 나가기
				rNum = token.nextToken();
				getOutRoom(rNum);
				break;
			case User.ECHO01: // 대기실 에코
				msg = token.nextToken();
				echoMsg(User.ECHO01 + "/" + user.toString() + msg);
				break;
			case User.ECHO02: // 채팅방 에코
				rNum = token.nextToken();
				msg = token.nextToken();
				echoMsg(rNum, msg);
				break;
			case User.GAMESTART: // 게임시작
				rNum = token.nextToken();
				GameStart(rNum);
				break;
			case User.GETINFO: // 게임시작
				rNum = token.nextToken();
				GetInfo(rNum);
				break;
			case User.WHISPER: // 귓속말
				id = token.nextToken();
				msg = token.nextToken();
				whisper(id, msg);
				break;
		}
	}

	private void GetInfo(String rNum) {
		String str="";
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				if(roomArray.get(i).getUserArray().get(0).getNickName().equals(this.user.getNickName())){//체크
					//나자신이니깐 할필요 x
					str = DB.searchRank(roomArray.get(i).getUserArray().get(1).getNickName());
				}
				else{
					str = DB.searchRank(roomArray.get(i).getUserArray().get(0).getNickName());
				}
			}
		}

		try {
			this.user.getDos().writeUTF(User.CHECKINFO+"/"+rNum+"/"+str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void getOutRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// 방에서 나가기
				// 채팅방의 유저리스트에서 사용자 삭제
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					if (user.getId().equals(
							roomArray.get(i).getUserArray().get(j).getId())) {
						roomArray.get(i).getUserArray().remove(j);
					}
				}

				// 사용자의 방리스트에서 방을 제거
				for (int j = 0; j < user.getRoomArray().size(); j++) {
					if (Integer.parseInt(rNum) == user.getRoomArray().get(j)
							.getRoomNum()) {
						user.getRoomArray().remove(j);
					}
				}
				echoMsg(roomArray.get(i), user.toString() + "님이 퇴장하셨습니다.");
				userList(rNum);

				if (roomArray.get(i).getUserArray().size() <= 0) {
					roomArray.remove(i);
					roomList();
				}
			}
		}
	}
	//방 입장
	private void getInRoom(String rNum) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				// 방 객체가 있는 경우, 방에 사용자추가
				// 사용자 객체에 방 추가
				if(roomArray.get(i).getUserArray().size()<2) {//2명미만 일경우
					roomArray.get(i).getUserArray().add(user);
					user.getRoomArray().add(roomArray.get(i));
					echoMsg(roomArray.get(i), user.toString() + "님이 입장하셨습니다.");
					userList(rNum);
				}
				else{
					try {
						user.getDos().writeUTF(User.FULLROOM);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	//방 만들기
	private void createRoom(String rNum, String rName) {
		Room rm = new Room(rName); // 지정한 제목으로 채팅방 생성
		rm.setMaker(user); // 방장 설정
		rm.setRoomNum(Integer.parseInt(rNum)); // 방번호 설정

		rm.getUserArray().add(user); // 채팅방에 유저(본인) 추가
		roomArray.add(rm); // 룸리스트에 현재 채팅방 추가
		user.getRoomArray().add(rm); // 사용자 객체에 접속한 채팅방을 저장

		echoMsg(User.ECHO01 + "/" + user.toString() + "님이 " + rm.getRoomNum()
				+ "번 채팅방을 개설하셨습니다.");
		echoMsg(rm, user.toString() + "님이 입장하셨습니다.");
		roomList();
		userList(rNum, thisUser);
		jta.append("성공 : " + userArray.toString() + "가 채팅방생성\n");
	}
	//귓속말
	private void whisper(String id, String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			if (id.equals(userArray.get(i).getId())) {
				// 귓속말 상대를 찾으면
				try {
					userArray
							.get(i)
							.getDos()
							.writeUTF(
									User.WHISPER + "/" + user.toProtocol()
											+ "/" + msg);
					jta.append("성공 : 귓속말보냄 : " + user.toString() + "가 "
							+ userArray.get(i).toString() + "에게" + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 대기실 에코
	private void echoMsg(String msg) {
		for (int i = 0; i < userArray.size(); i++) {
			try {
				userArray.get(i).getDos().writeUTF(msg);
				jta.append(user.toString() + " - " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("에러 : 에코 실패\n");
			}
		}
	}


	// 방 에코 (방 번호만 아는 경우)
	private void echoMsg(String rNum, String msg) {
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				echoMsg(roomArray.get(i), msg);
			}
		}
	}
	//게임시작
	private void GameStart(String rnum){
		if(user.getState()==0){
			user.setState(1);//현재 받아온 게임방의 유저가 게임준비완료 상태로 변경
			jta.append(user.getNickName()+"준비완료");
			for (int i = 0; i < roomArray.size(); i++) {
				if (Integer.parseInt(rnum) == roomArray.get(i).getRoomNum()) {
					if(roomArray.get(i).getUserArray().get(0).State==1&&roomArray.get(i).getUserArray().get(1).State==1){//동시에 state가 바뀐경우 게임작동
						//플레이어 번호 부여
						roomArray.get(i).getUserArray().get(0).playerNumber=0;
						roomArray.get(i).getUserArray().get(1).playerNumber=1;
						shuffle(i);
						System.out.println(roomArray.get(i).getUserArray().get(0).card[0]);
						roomArray.get(i).getUserArray().get(0).Card[0]=roomArray.get(i).getUserArray().get(0).card[0];
						roomArray.get(i).getUserArray().get(0).Card[1]=roomArray.get(i).getUserArray().get(0).card[1];
						roomArray.get(i).getUserArray().get(1).Card[0]=roomArray.get(i).getUserArray().get(1).card[0];
						roomArray.get(i).getUserArray().get(1).Card[1]=roomArray.get(i).getUserArray().get(1).card[1];
						jokbo(i,0);
						jokbo(i,1);

						final User[] users = new User[2];
						users[0] = roomArray.get(i).getUserArray().get(0);
						users[1] = roomArray.get(i).getUserArray().get(1);
						gameStart(users,i);
					}
				}
			}
		}
	}

	public void gameStart(User[] user,int rnum){
		String str = "";
		str = game(user, rnum);
		int winner = Integer.parseInt(str)-1;
		int Rnum1 = roomArray.get(rnum).getRoomNum();



		try {
			if (winner == 0) {
				DB.winRecord(roomArray.get(rnum).getUserArray().get(0).getNickName());//승리 전적
				DB.loseRecord(roomArray.get(rnum).getUserArray().get(1).getNickName());//패배 전적
				roomArray.get(rnum).getUserArray().get(0).getDos().writeUTF(User.GAMEWIN + "/" + Rnum1 + "/" + roomArray.get(rnum).getUserArray().get(0).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[1] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[1]+ "/" + roomArray.get(rnum).getUserArray().get(0).str);
				roomArray.get(rnum).getUserArray().get(1).getDos().writeUTF(User.GAMELOSE + "/" + Rnum1 + "/" + roomArray.get(rnum).getUserArray().get(1).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[1] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[1]+ "/" + roomArray.get(rnum).getUserArray().get(0).str);
			}
			else if(winner == 1) {
				DB.winRecord(roomArray.get(rnum).getUserArray().get(1).getNickName());//승리 전적
				DB.loseRecord(roomArray.get(rnum).getUserArray().get(0).getNickName());//패배 전적
				roomArray.get(rnum).getUserArray().get(0).getDos().writeUTF(User.GAMELOSE + "/" + Rnum1 + "/" + roomArray.get(rnum).getUserArray().get(0).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[1] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[1]+ "/" + roomArray.get(rnum).getUserArray().get(0).str);
				roomArray.get(rnum).getUserArray().get(1).getDos().writeUTF(User.GAMEWIN + "/" + Rnum1 + "/" + roomArray.get(rnum).getUserArray().get(1).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(1).Card[1] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[0] + "/" + roomArray.get(rnum).getUserArray().get(0).Card[1]+ "/" + roomArray.get(rnum).getUserArray().get(0).str);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

		roomArray.get(rnum).getUserArray().get(0).setState(0);;
		roomArray.get(rnum).getUserArray().get(1).setState(0);

	}
	//비기면 게임 다시 실행
	private void draw(User[] users, int rnum) {
		// 패 다시 돌리는 명령 해주고
		// 그후에 다시 돌리기
		shuffle(rnum);

		game(users,rnum);
	}
	//게임 조건 및 승자 체크
	private String game(User[] users, int rnum){
		int score = 0;
		int win=0;
		//sum 점수 불러와서
		for (int i = 0; i < users.length; i++) {
			if (score <= users[i].sum) {
				score = users[i].sum;
				win = i;
			}
		}

		if(score == 30){
			users[win].win++;
		}
		else if(score==29||score==28){
			for (int i = 0; i < users.length; i++) {
				if (users[i].level == 2) { // 암행어사
					users[i].win++;
					users[i].lose--;
					win = i;
					break;
				}
			}
			users[win].win++;
		}
		else if (score == 27) { // 멍구사
			draw(users, rnum); // 모든 사람 다시 시작
		} else if (score == 26) { // 장땡
			users[win].win++;
		} else if (17 <= score && score <= 25) { // 땡
			for (int i = 0; i < users.length; i++) {
				if (users[i].level == 1) { // 땡잡이
					users[i].win++;
					users[i].lose--;
					win = i;
					break;
				}
			}
			users[win].win++;
		} else if (score == 16) { // 구사
			draw(users, rnum);
		} else if (0 <= score && 15 <= score) {
			users[win].win++;
		}

		return String.valueOf(win+1);

	}
	//섯다 족보
	private void jokbo(int rnum,int player){

		if((roomArray.get(rnum).getUserArray().get(player).Card[0]==3&&roomArray.get(rnum).getUserArray().get(player).Card[1]==8)||(roomArray.get(rnum).getUserArray().get(player).Card[0]==8&&roomArray.get(rnum).getUserArray().get(player).Card[1]==3)){
			roomArray.get(rnum).getUserArray().get(player).level = 10;
			roomArray.get(rnum).getUserArray().get(player).sum = 30;
			roomArray.get(rnum).getUserArray().get(player).str = "삼팔광땡";
		}
		else if(roomArray.get(rnum).getUserArray().get(player).Card[0]==1 && (roomArray.get(rnum).getUserArray().get(player).Card[1]==3 || roomArray.get(rnum).getUserArray().get(player).Card[1]==8 ) || roomArray.get(rnum).getUserArray().get(player).Card[1]==1 && (roomArray.get(rnum).getUserArray().get(player).Card[0]==3 || roomArray.get(rnum).getUserArray().get(player).Card[0]==8)){
			roomArray.get(rnum).getUserArray().get(player).level=9;
			roomArray.get(rnum).getUserArray().get(player).sum = roomArray.get(rnum).getUserArray().get(player).Card[0]+roomArray.get(rnum).getUserArray().get(player).Card[1];
			switch(roomArray.get(rnum).getUserArray().get(player).sum){
				case 4:
					roomArray.get(rnum).getUserArray().get(player).str = "일삼광땡";
					roomArray.get(rnum).getUserArray().get(player).sum = 28;
				case 9:
					roomArray.get(rnum).getUserArray().get(player).str = "일팔광땡";
					roomArray.get(rnum).getUserArray().get(player).sum = 29;
			}
		}
		else if((roomArray.get(rnum).getUserArray().get(player).Card[0]==4&&roomArray.get(rnum).getUserArray().get(player).Card[1]==7)||(roomArray.get(rnum).getUserArray().get(player).Card[0]==7&&roomArray.get(rnum).getUserArray().get(player).Card[1]==4)){
			roomArray.get(rnum).getUserArray().get(player).level = 2;
			roomArray.get(rnum).getUserArray().get(player).str = "암행어사";//1,3광땡 18광떙 잡는 암행어사
		}
		else if((roomArray.get(rnum).getUserArray().get(player).Card[0]==4&&roomArray.get(rnum).getUserArray().get(player).Card[1]==9)||(roomArray.get(rnum).getUserArray().get(player).Card[0]==9&&roomArray.get(rnum).getUserArray().get(player).Card[1]==4)){
			roomArray.get(rnum).getUserArray().get(player).level = 4;
			roomArray.get(rnum).getUserArray().get(player).str = "멍텅구리 구사";
			roomArray.get(rnum).getUserArray().get(player).sum = 27;
		}
		else if ((roomArray.get(rnum).getUserArray().get(player).Card[0] % 10) == (roomArray.get(rnum).getUserArray().get(player).Card[1] % 10)) {
			roomArray.get(rnum).getUserArray().get(player).level = 7;
			switch (roomArray.get(rnum).getUserArray().get(player).Card[0] % 10) {
				case 1:
					roomArray.get(rnum).getUserArray().get(player).str = "일떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 1;
					roomArray.get(rnum).getUserArray().get(player).sum = 17;
					break;
				case 2:
					roomArray.get(rnum).getUserArray().get(player).str = "이떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 2;
					roomArray.get(rnum).getUserArray().get(player).sum = 18;
					break;
				case 3:
					roomArray.get(rnum).getUserArray().get(player).str = "삼떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 3;
					roomArray.get(rnum).getUserArray().get(player).sum = 19;
					break;
				case 4:
					roomArray.get(rnum).getUserArray().get(player).str = "사떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 4;
					roomArray.get(rnum).getUserArray().get(player).sum = 20;
					break;
				case 5:
					roomArray.get(rnum).getUserArray().get(player).str = "오떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 5;
					roomArray.get(rnum).getUserArray().get(player).sum = 21;
					break;
				case 6:
					roomArray.get(rnum).getUserArray().get(player).str = "육떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 6;
					roomArray.get(rnum).getUserArray().get(player).sum = 22;
					break;
				case 7:
					roomArray.get(rnum).getUserArray().get(player).str = "칠떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 7;
					roomArray.get(rnum).getUserArray().get(player).sum = 23;
					break;
				case 8:
					roomArray.get(rnum).getUserArray().get(player).str = "팔떙";
					roomArray.get(rnum).getUserArray().get(player).sum = 8;
					roomArray.get(rnum).getUserArray().get(player).sum = 24;
					break;
				case 9:
					roomArray.get(rnum).getUserArray().get(player).str = "구땡";
					roomArray.get(rnum).getUserArray().get(player).sum = 9;
					roomArray.get(rnum).getUserArray().get(player).sum = 25;
					break;
				case 0:
					roomArray.get(rnum).getUserArray().get(player).level = 8;
					roomArray.get(rnum).getUserArray().get(player).str = "장땡";
					roomArray.get(rnum).getUserArray().get(player).sum = 26;
					break;
			}
		}
		else if((roomArray.get(rnum).getUserArray().get(player).Card[0]==3 && roomArray.get(rnum).getUserArray().get(player).Card[1]==7) || (roomArray.get(rnum).getUserArray().get(player).Card[0]==7 && roomArray.get(rnum).getUserArray().get(player).Card[1]==3)){
			roomArray.get(rnum).getUserArray().get(player).level = 1;
			roomArray.get(rnum).getUserArray().get(player).str = "땡잡이";
		}
		else if ((roomArray.get(rnum).getUserArray().get(player).Card[0] % 10) == 1 || (roomArray.get(rnum).getUserArray().get(player).Card[1] % 10) == 1) {
			roomArray.get(rnum).getUserArray().get(player).level = 6;
			roomArray.get(rnum).getUserArray().get(player).sum = (roomArray.get(rnum).getUserArray().get(player).Card[0] + roomArray.get(rnum).getUserArray().get(player).Card[1]) % 10;
			switch (roomArray.get(rnum).getUserArray().get(player).sum) {
				case 3:
					roomArray.get(rnum).getUserArray().get(player).str = "알리";
					roomArray.get(rnum).getUserArray().get(player).sum = 15;
					break;
				case 5:
					roomArray.get(rnum).getUserArray().get(player).str = "독사";
					roomArray.get(rnum).getUserArray().get(player).sum = 14;
					break;
				case 0:
					roomArray.get(rnum).getUserArray().get(player).str = "구삥";
					roomArray.get(rnum).getUserArray().get(player).sum = 13;
					break;
				case 1:
					roomArray.get(rnum).getUserArray().get(player).str = "장삥";
					roomArray.get(rnum).getUserArray().get(player).sum = 12;
					break;
				default:
					roomArray.get(rnum).getUserArray().get(player).level = 0;
					roomArray.get(rnum).getUserArray().get(player).sum = (roomArray.get(rnum).getUserArray().get(player).Card[0] + roomArray.get(rnum).getUserArray().get(player).Card[1]) % 10;
					roomArray.get(rnum).getUserArray().get(player).str = roomArray.get(rnum).getUserArray().get(player).sum + "끝";
			}
		}
		else if ((roomArray.get(rnum).getUserArray().get(player).Card[0] % 10) == 4 || (roomArray.get(rnum).getUserArray().get(player).Card[1] % 10) == 4) {
			roomArray.get(rnum).getUserArray().get(player).level = 5;
			roomArray.get(rnum).getUserArray().get(player).sum = (roomArray.get(rnum).getUserArray().get(player).Card[0] + roomArray.get(rnum).getUserArray().get(player).Card[1]) % 10;
			switch (roomArray.get(rnum).getUserArray().get(player).sum) {
				case 4:
					roomArray.get(rnum).getUserArray().get(player).str = "장사";
					roomArray.get(rnum).getUserArray().get(player).sum = 11;
					break;
				case 0:
					roomArray.get(rnum).getUserArray().get(player).str = "세륙";
					roomArray.get(rnum).getUserArray().get(player).sum = 10;
					break;
				case 1:
					roomArray.get(rnum).getUserArray().get(player).level = 3;
					roomArray.get(rnum).getUserArray().get(player).str = "구사";
					roomArray.get(rnum).getUserArray().get(player).sum = 16;
					break;
				default:
					roomArray.get(rnum).getUserArray().get(player).level = 0;
					roomArray.get(rnum).getUserArray().get(player).sum = (roomArray.get(rnum).getUserArray().get(player).Card[0] + roomArray.get(rnum).getUserArray().get(player).Card[1]) % 10;
					roomArray.get(rnum).getUserArray().get(player).str = roomArray.get(rnum).getUserArray().get(player).sum + "끝";
			}
		}
		else {
			roomArray.get(rnum).getUserArray().get(player).level = 0;
			roomArray.get(rnum).getUserArray().get(player).sum = (roomArray.get(rnum).getUserArray().get(player).Card[0] + roomArray.get(rnum).getUserArray().get(player).Card[1]) % 10;
			roomArray.get(rnum).getUserArray().get(player).str = roomArray.get(rnum).getUserArray().get(player).sum + "끝";
			if (roomArray.get(rnum).getUserArray().get(player).sum == 0){
				roomArray.get(rnum).getUserArray().get(player).str = "망통";
			}
		}

	}
	//카드 섞기
	private void shuffle(int rnum){
		int w,r;
		w=0;
		while(w<2){
			r=(int)(Math.random()*10)+1;
			roomArray.get(rnum).getUserArray().get(0).card[w]=r;
			w++;
		}
		w=0;
		//플레이어 2 카드설정
		while(w<2){
			r=(int)(Math.random()*10)+1;
			roomArray.get(rnum).getUserArray().get(1).card[w]=r;
			w++;
		}
	}



	// 방 에코 (방객체가 있는 경우)
	private void echoMsg(Room room, String msg) {
		for (int i = 0; i < room.getUserArray().size(); i++) {
			try {
				// 방에 참가한 유저들에게 에코 메시지 전송
				room.getUserArray()
						.get(i)
						.getDos()
						.writeUTF(
								User.ECHO02 + "/" + room.getRoomNum() + "/"
										+ msg);
				jta.append("성공 : 메시지전송 : " + msg + "\n");
			} catch (IOException e) {
				e.printStackTrace();
				jta.append("에러 : 에코 실패\n");
			}
		}
	}

	private void member(String name,String Nname,String id, String pw,String email) {
		User newUser = new User();
		newUser.setId(id);
		newUser.setPw(pw);
		newUser.setNickName(Nname);
		newUser.setName(name);
		newUser.setEmail(email);

		try {
			boolean flag = DB.joinCheck(name,Nname,id,pw,email);
			if (flag==true) {
				thisUser.writeUTF(User.MEMBERSHIP + "/OK");
				jta.append("성공 : 회원가입 파일생성\n");
			} else {
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
				jta.append("에러 : 이미 가입된 회원\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				thisUser.writeUTF(User.MEMBERSHIP + "/fail");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			jta.append("에러 : 회원가입 파일생성\n");
		}
	}

	private void login(String id, String pw) {
		int inputValue = 0;
		try {
			// 파일 열기

			try {
				if(!DB.loginCheck(id,pw).equals("null")) {
					if (!DB.loginCheck(id,pw).equals("null")) {
						for (int i = 0; i < userArray.size(); i++) {
							if (id.equals(userArray.get(i).getId())) {
								try {
									System.out.println("접속중");
									thisUser.writeUTF(User.LOGIN
											+ "/fail/이미 접속 중입니다.");
								} catch (IOException e) {
									e.printStackTrace();
								}
								return;
							}
						}

						// 로그인 OK
						user.setId(id);
						user.setPw(pw);
						user.setNickName(DB.loginCheck(id,pw));
						thisUser.writeUTF(User.LOGIN + "/OK/"
								+ user.getNickName());
						this.user.setOnline(true);

						// 대기실에 에코
						echoMsg(User.ECHO01 + "/" + user.toString()
								+ "님이 입장하셨습니다.");
						jta.append(id + " : 님이 입장하셨습니다.\n");

						roomList(thisUser);
						for (int i = 0; i < userArray.size(); i++) {
							userList(userArray.get(i).getDos());
						}
					} else {
						thisUser.writeUTF(User.LOGIN + "/fail/패스워드가 일치하지 않습니다.");
						jta.append("에러 : 로그인-패스워드가 일치하지 않습니다. : " + pw + "\n");
					}
				} else {
					thisUser.writeUTF(User.LOGIN + "/fail/아이디가 존재하지 않습니다.");
					jta.append("에러 : 로그인-아이디가 일치하지 않습니다. : " + id + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
				thisUser.writeUTF(User.LOGIN + "/fail/로그인실패");
				jta.append("에러 : 로그인 실패" + pw + "\n");
			}
		}
		catch (Exception e) {
			try {
				thisUser.writeUTF(User.LOGIN + "/fail/아이디가 존재하지 않습니다.");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
	}

	private void logout() {
		System.out.println("로그아웃");

		// 오프라인으로 바꿈
		user.setOnline(false);
		// 사용자배열에서 삭제
		for (int i = 0; i < userArray.size(); i++) {
			if (user.getId().equals(userArray.get(i).getId())) {
				System.out.println(userArray.get(i).getId() + "지웠다.");
				userArray.remove(i);
			}
		}
		// room 클래스의 멤버변수인 사용자배열에서 삭제
		for (int i = 0; i < roomArray.size(); i++) {
			for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
				if (user.getId().equals(
						roomArray.get(i).getUserArray().get(j).getId())) {
					roomArray.get(i).getUserArray().remove(j);
				}
			}
		}
		echoMsg(User.ECHO01 + "/" + user.toString() + "님이 퇴장하셨습니다.");

		for (int i = 0; i < userArray.size(); i++) {
			userList(userArray.get(i).getDos());
		}

		jta.append(user.getId() + " : 님이 퇴장하셨습니다.\n");

		try {
			user.getDos().writeUTF(User.LOGOUT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			user.getDis().close();
			user.getDos().close();
			user = null;
			jta.append("성공 : 스트림 닫기\n");
		} catch (IOException e) {
			e.printStackTrace();
			jta.append("실패 : 스트림 닫기\n");
		}
	}

	// 사용자 리스트 (선택한 채팅방)
	public void selectedRoomUserList(String rNum, DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// 채팅방에 접속되어 있는 유저들의 아이디+닉네임
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
							.toProtocol();
				}
			}
		}
		try {
			// 데이터 전송
			target.writeUTF(User.UPDATE_SELECTEDROOM_USERLIST + ul);
			jta.append("성공 : 목록(사용자)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("에러 : 목록(사용자) 전송 실패\n");
		}
	}

	// 사용자 리스트 (대기실)
	public String userList(DataOutputStream target) {
		String ul = "";

		for (int i = 0; i < userArray.size(); i++) {
			// 접속되어 있는 유저들의 아이디+닉네임
			ul += "/" + userArray.get(i).toProtocol();
		}

		try {
			// 데이터 전송
			target.writeUTF(User.UPDATE_USERLIST + ul);
			jta.append("성공 : 목록(사용자)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("에러 : 목록(사용자) 전송 실패\n");
		}
		return ul;
	}

	// 사용자 리스트 (채팅방 내부)
	public void userList(String rNum, DataOutputStream target) {
		String ul = "/" + rNum;

		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// 채팅방에 접속되어 있는 유저들의 아이디+닉네임
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
							.toProtocol();
				}
			}
		}
		try {
			// 데이터 전송
			target.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
			jta.append("성공 : 목록(사용자)-" + ul + "\n");
		} catch (IOException e) {
			jta.append("에러 : 목록(사용자) 전송 실패\n");
		}
	}

	// 사용자 리스트 (채팅방 내부 모든 사용자들에게 전달)
	public void userList(String rNum) {
		String ul = "/" + rNum;
		Room temp = null;
		for (int i = 0; i < roomArray.size(); i++) {
			if (Integer.parseInt(rNum) == roomArray.get(i).getRoomNum()) {
				temp = roomArray.get(i);
				for (int j = 0; j < roomArray.get(i).getUserArray().size(); j++) {
					// 채팅방에 접속되어 있는 유저들의 아이디+닉네임
					ul += "/"
							+ roomArray.get(i).getUserArray().get(j)
							.toProtocol();
				}
			}
		}
		for (int i = 0; i < temp.getUserArray().size(); i++) {
			try {
				// 데이터 전송
				temp.getUserArray().get(i).getDos()
						.writeUTF(User.UPDATE_ROOM_USERLIST + ul);
				jta.append("성공 : 목록(사용자)-" + ul + "\n");
			} catch (IOException e) {
				jta.append("에러 : 목록(사용자) 전송 실패\n");
			}
		}
	}

	// 채팅 방리스트
	public void roomList(DataOutputStream target) {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// 만들어진 채팅방들의 제목
			rl += "/" + roomArray.get(i).toProtocol();
		}

		jta.append("test\n");

		try {
			// 데이터 전송
			target.writeUTF(User.UPDATE_ROOMLIST + rl);
			jta.append("성공 : 목록(방)-" + rl + "\n");
		} catch (IOException e) {
			jta.append("에러 : 목록(방) 전송 실패\n");
		}
	}

	// 채팅 방리스트
	public void roomList() {
		String rl = "";

		for (int i = 0; i < roomArray.size(); i++) {
			// 만들어진 채팅방들의 제목
			rl += "/" + roomArray.get(i).toProtocol();
		}

		jta.append("test\n");

		for (int i = 0; i < userArray.size(); i++) {

			try {
				// 데이터 전송
				userArray.get(i).getDos().writeUTF(User.UPDATE_ROOMLIST + rl);
				jta.append("성공 : 목록(방)-" + rl + "\n");
			} catch (IOException e) {
				jta.append("에러 : 목록(방) 전송 실패\n");
			}
		}
	}

	//게임 관련 함수들
	private void setJokbo() {
		jokbo[13][18] = Integer.MAX_VALUE;//38광떙
		jokbo[11][13] = 10000;//13광땡
		jokbo[11][10] = 10000;//18광땡

		for(int i=1;i<11;i++){
			for(int j=11;j<21;j++){
				int num = (i+j)%10;
				jokbo[i][j] = num;
				if(i+10==j){
					jokbo[i][j]+=1000;
				}
			}
		}

		jokbo[10][20] = jokbo[10][20]+1100;
		//알리 1,2
		jokbo[1][12] = jokbo[1][12] + 900;
		jokbo[1][2] = jokbo[1][2] + 900;
		jokbo[2][11] = jokbo[2][11] + 900;
		jokbo[11][12] = jokbo[11][12] + 900;
		//독사 1,4
		jokbo[1][14] = jokbo[1][14] + 800;
		jokbo[11][14] = jokbo[11][14] + 800;
		jokbo[1][4] = jokbo[1][4] + 800;
		jokbo[4][11] = jokbo[4][11] + 800;
		//구삥 1,9
		jokbo[1][19] = jokbo[1][19] + 700;
		jokbo[1][9] = jokbo[1][9] + 700;
		jokbo[9][11] = jokbo[9][11] + 700;
		jokbo[11][19] = jokbo[11][19] + 700;
		//장삥 1,10
		jokbo[1][20] = jokbo[1][20] + 600;
		jokbo[1][10] = jokbo[1][10] + 600;
		jokbo[10][11] = jokbo[10][11] + 600;
		jokbo[11][20] = jokbo[11][20] + 600;
		//장사 10,4
		jokbo[10][14] = jokbo[10][14] + 500;
		jokbo[4][10] = jokbo[4][10] + 500;
		jokbo[4][20] = jokbo[4][20] + 500;
		jokbo[14][20] = jokbo[14][20] + 500;
		//세륙
		jokbo[6][14] = jokbo[6][14] + 400;
		jokbo[4][6] = jokbo[4][6] + 400;
		jokbo[4][16] = jokbo[4][16] + 400;
		jokbo[14][16] = jokbo[14][16] + 400;
	}
}
