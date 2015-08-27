# Flume Receiver Intent-Based Sender Push Feature

## �T�v

`flume-risp` �� **Receiver Intent-based Sender Push ���b�Z�[�W�z�M���f��**�� Apache Flume Source/Sink �����ł��B
�]���̃N���C�A���g/�T�[�o�^�݌v�Ɠ��l�ɁA�f�[�^��M�� (ClientSource) ���K�v�ɉ����đ��M�� (ServerSink) �Ɛڑ���
�A�h�z�b�N�ȃf�[�^���W���s���܂��B����ɂ��f�[�^�̎�M������ɋ쓮���Ă����ԂłȂ��Ă��A�K�v�ȂƂ��ɂ��� Flume ����
�f�[�^�����W����^�p���s�����Ƃ��ł��܂��B

`flume-rips` �̓f�[�^�Z���^�[������t�@�C�A�E�H�[���⃍�[�h�o�����T����ĊO���Ƀf�[�^�𑗐M�ł���悤 Flume �C�x���g��
WebSockets��œ]�����܂��B

Sink �� WebSocket �T�[�o�Ƃ��Ď�������Ă���AFlume �f�[�^�]����v������N���C�A���g����ڑ����邱�Ƃɂ���ăN���C�A���g
(�ʂ� Flume Agent) �ɑ΂��Ẵf�[�^�]�����J�n���܂��B

## ����

* WebSockets �����̑I��ɂ���: Finagle �� WebSockets �������Ȃ��BNetty �̓o�[�W���� 4 �� WebSockets �n���h�V�F�C�N���������Ă��邪 Flume 1.6 �� Netty 3.5 �Ɉˑ����Ă���BGrizzly�AJetty �� WebSockets �N���C�A���g/�T�[�o���Ɏ�������B
* TLS (wss) �Ή��B
