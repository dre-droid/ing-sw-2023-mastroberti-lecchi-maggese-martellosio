package Server;

public class ClientNotificationRecord {
        public String nickname;
        ClientNotificationInterfaceRMI client;

        public ClientNotificationRecord(String nickname, ClientNotificationInterfaceRMI client){
            this.nickname = nickname;
            this.client = client;
        }
}
