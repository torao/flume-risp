/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.koiroha.flume.risp

import java.net.InetSocketAddress

import org.apache.flume.event.SimpleEvent
import org.specs2.Specification

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DispatcherSpec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
class DispatcherSpec extends Specification { def is = s2"""
Dispatcher should:
not send event without any connections. $st
"""

  def st = {
	  val addr = new InetSocketAddress(0)
	  val dispatcher = new Dispatcher(addr, "/api/1.0/logs")
	  dispatcher.start()
	  dispatcher.deliver(new SimpleEvent())
	  Await.result(dispatcher.shutdown(), Duration.Inf)
	  success
  }
}
