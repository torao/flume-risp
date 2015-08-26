# Flume Receiver Intent-Based Sender Push Feature

## �T�v

`flume-risp` �� **Receiver Intent-based Sender Push Message Delivery** �� Source/Sink �����ł��B
�t�@�C�A�E�H�[���⃍�[�h�o�����T�Ȃǂ���݂���f�[�^�Z���^�[����O���փA�h�z�b�N�ȃf�[�^�]�����s�����Ƃ�ړI�Ƃ��Ă��܂��B

�f�[�^�̓]���悪�s��ȏꍇ

Sink �� WebSocket �T�[�o�Ƃ��Ď�������Ă���AFlume �f�[�^�]����v������N���C�A���g����ڑ����邱�Ƃɂ���ăN���C�A���g
(�ʂ� Flume Agent) �ɑ΂��Ẵf�[�^�]�����J�n���܂��B

`flume-risp` contains Source/Sink features based on **Receiver Intent-based Sender Push** message delivery model.

Uses WebSockets to beyond Firewall or HTTP load-balancer .

## ����

* WebSockets �����̑I��ɂ���: Finagle �� WebSockets �������Ȃ��BNetty �̓o�[�W���� 4 �� WebSockets �n���h�V�F�C�N���������Ă��邪 Flume 1.6 �� Netty 3.5 �Ɉˑ����Ă���BGrizzly�AJetty �� WebSockets �N���C�A���g/�T�[�o���Ɏ�������B
