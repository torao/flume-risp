/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ClientSource
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
public class ClientSource extends AbstractSource implements EventDrivenSource, Configurable{
    private static final Logger logger = LoggerFactory.getLogger(ClientSource.class);

    /**
     * The URL to connect from this source. It is expected to serve by {@link ServerSink
     * flume-risp sink}.
     */
    private URI uri = null;

    /**
     * TCP connect timeout by milliseconds.
     */
    private int connectTimeout = 30 * 1000;

    /**
     */
    private Receiver receiver = null;

    /**
     * Default constructor is used to create instance dynamically.
     */
    public ClientSource(){ /* */ }

    @Override
    public void configure(Context context) {
        String host = context.getString("server.risp.host", "localhost");
        int port = context.getInteger("server.risp.port", 8011);
        String path = context.getString("server.risp.path", "/ws/flume");
        try {
            this.uri = new URL("http", host, port, path).toURI();
        } catch(MalformedURLException | URISyntaxException ex){
            logger.error("invalid hostname or path: host=" + host + ", path=" + path, ex);
            throw new IllegalArgumentException(ex);
        }
        this.connectTimeout = context.getInteger("server.risp.connectTimeout", 30) * 1000;
        logger.debug("risp server: " + this.uri);
    }

    @Override
    public void start() {
        assert(receiver == null);
        receiver = new Receiver(uri, e -> getChannelProcessor().processEvent(e), connectTimeout);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        assert(receiver != null);
        receiver.close();
        receiver = null;
    }
}
