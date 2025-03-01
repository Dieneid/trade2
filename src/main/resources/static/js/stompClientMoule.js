
export default class StompClientModule {

    constructor(serverUrl) {
        this.serverUrl = serverUrl;
        this.stompClient = null;
    }

    connect(onMessageReceivedCallback) {
        const socket = new SockJS(this.serverUrl);
        this.stompClient = Stomp.over(socket);
        return new Promise((resolve, reject) => {
            this.stompClient.connect({}, () => {
                this.stompClient.subscribe('/user/sub/messages', (message) => {
                    const msg = JSON.parse(message.body);
                    if (onMessageReceivedCallback) {
                        onMessageReceivedCallback(msg);
                    }
                });


                resolve();
            }, (error) => {
                console.error('Connection failed', error);
                reject(error);
            });
        });
    }

    sendMessage(sender,receiver, content) {
        if (!this.stompClient || !this.stompClient.connected) {
            return;
        }
        const message = {
            sender: sender,
            receiver: receiver,
            content: content,
            timestamp: new Date().toISOString(),
        };
        this.stompClient.send('/pub/send', {}, JSON.stringify(message));
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect(() => {
                console.log('Disconnected from the server');
            });
        }
    }
}
