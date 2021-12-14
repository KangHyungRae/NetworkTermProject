package mew;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBase {
    Connection con = null;
    Statement stmt = null;
    String url = "jdbc:mysql://127.0.0.1:3306/seotda";
    String user = "root";
    String passwd = "12345";

    public DataBase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.con = DriverManager.getConnection(this.url, this.user, this.passwd);
            this.stmt = this.con.createStatement();
            System.out.println("[Server] MySQL 서버 연동 성공");
        } catch (Exception var2) {
            System.out.println("[Server] MySQL 서버 연동 실패> " + var2.toString());
        }

    }

    public String loginCheck(String _i, String _p) {
        String nickname = "null";
        String id = _i;
        String pw = _p;

        try {
            String checkingStr = "SELECT password, nickname FROM member WHERE id='" + id + "'";
            ResultSet result = this.stmt.executeQuery(checkingStr);

            for(int var8 = 0; result.next(); ++var8) {
                if (pw.equals(result.getString("password"))) {
                    nickname = result.getString("nickname");
                    System.out.println("[Server] 로그인 성공");
                } else {
                    nickname = "null";
                    System.out.println("[Server] 로그인 실패");
                }
            }
        } catch (Exception var9) {
            nickname = "null";
            System.out.println("[Server] 로그인 실패 > " + var9.toString());
        }

        return nickname;
    }

    public boolean joinCheck(String _n, String _nn, String _i, String _p, String _e) {
        boolean flag = false;
        String na = _n;
        String nn = _nn;
        String id = _i;
        String pw = _p;
        String em = _e;
        boolean check = this.overCheck(_i, _p);
        if (!check) {
            try {
                String insertStr = "INSERT INTO member VALUES('" + na + "', '" + nn + "', '" + id + "', '" + pw + "', '" + em + "', 0, 0)";
                this.stmt.executeUpdate(insertStr);
                flag = true;
                System.out.println("[Server] 회원가입 성공");
            } catch (Exception var14) {
                flag = false;
                System.out.println("[Server] 회원가입 실패 > " + var14.toString());
            }
        }

        return flag;
    }

    boolean overCheck(String _a, String _v) {
        boolean flag = false;
        String att = _a;
        String val = _v;

        try {
            String selcectStr = "SELECT id FROM member WHERE id= " + att;
            ResultSet result = this.stmt.executeQuery(selcectStr);

            for(int var8 = 0; result.next(); ++var8) {
                if (!val.equals(result.getString(att))) {
                    flag = true;
                } else {
                    flag = false;
                }
            }

            System.out.println("[Server] 중복 확인 성공");
        } catch (Exception var9) {
            System.out.println("[Server] 중복 확인 실패 > " + var9.toString());
        }

        return flag;
    }

    boolean winRecord(String _nn) {
        boolean flag = false;	//참거짓을 반환할 flag 변수. 초기값은 false.

        //매개변수로 받은 닉네임과 조회한 승리 횟수를 저장할 변수. num의 초기값은 0.
        String nick = _nn;
        int num = 0;

        try {
            //member 테이블에서 nick이라는 닉네임을 가진 회원의 승리 횟수를 조회한다.
            String searchStr = "SELECT win FROM member WHERE nickname='" + nick + "'";
            ResultSet result = stmt.executeQuery(searchStr);

            int count = 0;
            while(result.next()) {
                //num에 조회한 승리 횟수를 초기화.
                num = result.getInt("win");
                count++;
            }
            num++;	//승리 횟수를 올림

            //member 테이블에서 nick이라는 닉네임을 가진 회원의 승리 횟수를 num으로 업데이트한다.
            String changeStr = "UPDATE member SET win=" + num + " WHERE nickname='" + nick +"'";
            stmt.executeUpdate(changeStr);
            flag = true;	//조회 및 업데이트 성공 시 flag를 true로 바꾸고 성공을 콘솔로 알린다.
            System.out.println("[Server] 전적 업데이트 성공");
        } catch(Exception e) {	//조회 및 업데이트 실패 시 flag를 false로 바꾸고 실패를 콘솔로 알린다.
            flag = false;
            System.out.println("[Server] 전적 업데이트 실패 > " + e.toString());
        }

        return flag;	//flag 반환
    }

    boolean loseRecord(String _nn) {
        boolean flag = false;	//참거짓을 반환할 flag 변수. 초기값은 false.

        //매개변수로 받은 닉네임과 조회한 패배 횟수를 저장할 변수. num의 초기값은 0.
        String nick =  _nn;
        int num = 0;

        try {
            //member 테이블에서 nick이라는 닉네임을 가진 회원의 패배 횟수를 조회한다.
            String searchStr = "SELECT lose FROM member WHERE nickname='" + nick + "'";
            ResultSet result = stmt.executeQuery(searchStr);

            int count = 0;
            while(result.next()) {
                //num에 조회한 패배 횟수를 초기화.
                num = result.getInt("lose");
                count++;
            }
            num++;	//패배 횟수를 올림

            //member 테이블에서 nick이라는 닉네임을 가진 회원의 승리 횟수를 num으로 업데이트한다.
            String changeStr = "UPDATE member SET lose=" + num + " WHERE nickname='" + nick +"'";
            stmt.executeUpdate(changeStr);
            flag = true;	//조회 및 업데이트 성공 시 flag를 true로 바꾸고 성공을 콘솔로 알린다.
            System.out.println("[Server] 전적 업데이트 성공");
        } catch(Exception e) {	//조회 및 업데이트 실패 시 flag를 false로 바꾸고 실패를 콘솔로 알린다.
            flag = false;
            System.out.println("[Server] Error: > " + e.toString());
        }

        return flag;	//flag 반환
    }

    String searchRank(String _nn) {
        String msg = "null";	//전적을 받을 문자열. 초기값은 "null"로 한다.

        //매개변수로 받은 닉네임을 초기화한다.
        String nick = _nn;

        try {
            //member 테이블에서 nick이라는 닉네임을 가진 회원의 승, 패를 조회한다.
            String searchStr = "SELECT win, lose FROM member WHERE nickname='" + nick + "'";
            ResultSet result = stmt.executeQuery(searchStr);

            int count = 0;
            while(result.next()) {
                //msg에 "닉네임 : n승 n패" 형태의 문자열을 초기화한다.
                msg = result.getInt("win") + "/" + result.getInt("lose");
                count++;
            }
            System.out.println("[Server] 전적 조회 성공");	//정상적으로 수행되면 성공을 콘솔로 알린다.
        } catch(Exception e) {	//정상적으로 수행하지 못하면 실패를 콘솔로 알린다.
            System.out.println("[Server] 전적 조회 실패 > " + e.toString());
        }

        return msg;	//msg 반환
    }




}

