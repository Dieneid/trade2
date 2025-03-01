package greenart.trade.mebmer.service;


import com.sun.mail.imap.IMAPSSLStore;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.Properties;

public class EmailProcessor {

    private Date lastCheckedDate; // 클릭 시점 저장
    private String latestPhoneNumber; // 최근 추출된 전화번호
    private String latestCarrier; // 최근 추출된 통신사

    public void setLastCheckedDate(Date date) {
        this.lastCheckedDate = date;
    }

    public void checkEmails(int randomCode) {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", "imap.gmail.com");
            props.put("mail.imaps.port", "");
            props.put("mail.imaps.ssl.enable", "true");

            // 세션 생성
            Session session = Session.getInstance(props, null);

            // 프로바이더 수동 등록
            session.setProvider(new Provider(Provider.Type.STORE, "imaps", IMAPSSLStore.class.getName(), "Oracle", "1.6.7"));

            // IMAP Store 생성 및 연결
            Store store = session.getStore("imaps");
            store.connect("dieneid13@gmail.com", "");

            System.out.println("IMAP 연결 성공!");

            // INBOX 열기
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            if (lastCheckedDate == null) {
                System.out.println("lastCheckedDate가 설정되지 않았습니다.");
                return;
            }
            System.out.println("검색 기준 시점: " + lastCheckedDate);

            // 최근 10개의 이메일만 검색
            int totalMessages = inbox.getMessageCount();
            int start = Math.max(1, totalMessages - 10); // 최근 10개의 이메일만 검색
            Message[] messages = inbox.getMessages(start, totalMessages);


            for (Message message : messages) {
                Date receivedDate = message.getReceivedDate();
                if (receivedDate != null && receivedDate.after(lastCheckedDate)) {
                    InternetAddress sender = (InternetAddress) message.getFrom()[0];

                    String subject = message.getSubject();
                    int number = Integer.parseInt(subject);

                    if(number == randomCode){
                        // 발신자 이메일에서 전화번호 추출
                        String phoneNumber = extractPhoneNumber(sender.getAddress());
                        String carrier = identifyCarrier(sender.getAddress());

                        if (phoneNumber != null && !carrier.equals("Unknown")) {
                            // 추출된 정보를 DB에 저장
                            saveToDatabase(phoneNumber, carrier);

                            // 최신 정보 업데이트
                            latestPhoneNumber = phoneNumber;
                            latestCarrier = carrier;

                            System.out.println("전화번호: " + phoneNumber + ", 통신사: " + carrier);
                        } else {
                            System.out.println("전화번호 또는 통신사 정보를 추출할 수 없습니다.");
                        }
                    }

                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractPhoneNumber(String email) {
        if (email.contains("@")) {
            return email.split("@")[0]; // '@' 앞부분이 전화번호
        }
        return null;
    }

    private String identifyCarrier(String email) {
        if (email.endsWith("@mmsmail.uplus.co.kr")) return "LG U+";
        if (email.endsWith("@ktfmms.magicn.com")) return "KT";
        if (email.endsWith("@vmms.nate.com")) return "SKT";
        return "Unknown";
    }

    private void saveToDatabase(String phoneNumber, String carrier) {
        System.out.println("Saving to database: " + phoneNumber + ", " + carrier);
    }

    public String getLatestPhoneNumber() {
        return latestPhoneNumber;
    }

    public String getLatestCarrier() {
        return latestCarrier;
    }
}
