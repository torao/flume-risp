# Flume Receiver Intent-Based Sender Push Feature

## 概要

`flume-risp` は **Receiver Intent-based Sender Push Message Delivery** の Source/Sink 実装です。
ファイアウォールやロードバランサなどが介在するデータセンターから外部へアドホックなデータ転送を行うことを目的としています。

データの転送先が不定な場合

Sink は WebSocket サーバとして実装されており、Flume データ転送を要求するクライアントから接続することによってクライアント
(別の Flume Agent) に対してのデータ転送を開始します。

`flume-risp` contains Source/Sink features based on **Receiver Intent-based Sender Push** message delivery model.

Uses WebSockets to beyond Firewall or HTTP load-balancer .

## メモ

* WebSockets 実装の選定について: Finagle は WebSockets 実装がない。Netty はバージョン 4 で WebSockets ハンドシェイクを実装しているが Flume 1.6 は Netty 3.5 に依存している。Grizzly、Jetty は WebSockets クライアント/サーバ共に実装あり。
