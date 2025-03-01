import StompClientModule from './stompClientMoule.js';

document.addEventListener('DOMContentLoaded', () => {

    const serverUrl = '/ws'; // 서버 WebSocket 엔드포인트
    const stompModule = new StompClientModule(serverUrl);

    // 메시지 수신 시 처리
    const onMessageReceived = (message) => {
        const sender = message.sender;
        const receiver = message.receiver
        const content =  message.content
        $('#messageReceiveForm .sender').val(sender)
        $('#messageReceiveForm .content').val(content)
        $('#messageReceiveForm').modal('show');
    };

    // 서버에 연결
    stompModule.connect(onMessageReceived)
        .then(() => {
            $('.sendButton').click(function(){
                const sender = $('.username').val();
                const receiver = $('#sendMessageForm .receiver').val(); // #sendMessageForm .receiver
                const content = $('#sendMessageForm .content').val(); // #sendMessageForm .content
                stompModule.sendMessage(sender, receiver, content);
                $('#sendMessageForm .content').val(''); // 메시지 전송후  입력란 초기화
            });
        })
        .catch((error) => {
            console.error('Failed to connect to the WebSocket server', error);
        });

    // 페이지 종료 시 연결 해제
    window.addEventListener('beforeunload', () => {
        stompModule.disconnect();
    });
});
