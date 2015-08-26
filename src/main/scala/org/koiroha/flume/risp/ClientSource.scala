/*
 * Copyright (c) 2015 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.{SocketAddress, InetSocketAddress}
import javax.ws.rs.client.ClientBuilder

import org.apache.flume.Context
import org.apache.flume.PollableSource.Status
import org.apache.flume.source.AbstractPollableSource

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ClientSource
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class ClientSource extends AbstractPollableSource {
	case class Config(address:SocketAddress, path:String)

	private[this] var config:Option[Config] = None

	override def doConfigure(context: Context): Unit = {
		this.config = Some(Config(
			new InetSocketAddress(
				context.getString("server.url"),
				context.getInteger("server.http.port", 8011)),
		  context.getString("server.http.path", "/ws/flume")
		))

	}

	override def doProcess(): Status = ???

	override def doStop(): Unit = ???

	override def doStart(): Unit = {
		val client = ClientBuilder.newClient()
		client.ta
	}

}
