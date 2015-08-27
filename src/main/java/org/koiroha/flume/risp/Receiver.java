package org.koiroha.flume.risp;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;
import com.ning.http.client.ws.DefaultWebSocketListener;
import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;
import org.apache.flume.Event;
import org.apache.flume.event.SimpleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Receiver
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Low-level protocol implementation to receive flume event.
 *
 * @author Takami Torao
 */
class Receiver implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    /**
     * URL to the server that serves flume {@link ServerSink}.
     */
    private final URI uri;

    /**
     * Asynchronous HTTP client that has a scope from start to stop.
     */
    private final AsyncHttpClient client;

    /**
     * Currently connected WebSockets, that has a scope from start to stop.
     */
    private final AtomicReference<WebSocket> websocket = new AtomicReference<>(null);

    /**
     *
     */
    private final Consumer<Event> receiver;

    private final WebSocketListener listener = new DefaultWebSocketListener() {
        @Override
        public void onMessage(byte[] msg) {
            try {
                RispEvent.FlumeEvent e = RispEvent.FlumeEvent.parseFrom(msg);
                Event fe = new SimpleEvent();
                fe.setBody(e.getBody().toByteArray());
                fe.setHeaders(e.getHeadersList().stream().collect(Collectors.toMap(RispEvent.FlumeEvent.HeaderField::getName, RispEvent.FlumeEvent.HeaderField::getValue)));
                receiver.accept(fe);
            } catch (InvalidProtocolBufferException e1) {
                e1.printStackTrace();
            }
        }
        @Override
        public void onError(Throwable ex) {
            logger.error("", ex);
            connect();
        }
    };

    /**
     * URI must have {@code ws} or {@code wss} scheme.
     *
     * @param uri URI to risp {@link ServerSink server sink}
     * @param connectTimeout TCP connect timeout by milliseconds
     */
    public Receiver(URI uri, Consumer<Event> receiver, int connectTimeout){
        if(! "ws".equalsIgnoreCase(uri.getScheme()) || ! "wss".equalsIgnoreCase(uri.getScheme())){
            throw new IllegalArgumentException("risp url must have scheme 'ws' or 'wss'");
        }
        this.uri = uri;
        this.receiver = receiver;

        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setConnectTimeout(connectTimeout)
                .build();
        this.client = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(config), config);
        connect();
    }

    public void close() {
        closeWebSocket();
        client.close();
    }

    /**
     *
     * reconnect if this already connected
     */
    private void connect() {

        // close current websocket if present
        closeWebSocket();

        WebSocketUpgradeHandler handler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build();
        WebSocket websocket = null;
        long backoff = 20;
        try {
            // retry connect while success, or closed
            do {
                try {
                    websocket = client.prepareGet(uri.toString()).execute(handler).get(30, TimeUnit.SECONDS);
                } catch (ExecutionException | TimeoutException ex) {
                    logger.error("connect failure: url=" + uri + ", retry after " + backoff + "ms", ex);
                    Thread.sleep(backoff);
                    backoff = Math.min(backoff * 2, 10 * 1000L);
                }
            } while (websocket == null && Thread.currentThread().isInterrupted() && ! client.isClosed());
        } catch(InterruptedException e) {
            logger.warn("operation interrupted");
            return;
        }

        if(websocket != null && ! this.websocket.compareAndSet(null, websocket)){
            websocket.close();
        }
    }

    /**
     * close websocket
     */
    private void closeWebSocket() {
        WebSocket old = this.websocket.getAndSet(null);
        if(old != null){
            old.close();
        }
    }

}
