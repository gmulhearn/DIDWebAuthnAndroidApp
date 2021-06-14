var clientPeer = new SimplePeer({trickle: false});

clientPeer.on('signal', data => {
    webrtcInterface.signalled(JSON.stringify(data));
});

clientPeer.on('data', data => {
    webrtcInterface.dataReceived(data.toString());
});

const signalPeer = (dataStr) => {
    var data = dataStr;
    clientPeer.signal(data);
}

const sendData = (dataStr) => {
    clientPeer.send(dataStr);
}