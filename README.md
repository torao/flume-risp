# Flume Receiver Intent-Based Sender Push Feature

## �T�v

`flume-risp` �� **Receiver Intent-based Sender Push ���b�Z�[�W�z�M���f��**�� Apache Flume Source/Sink �����ł��B
�]���̃N���C�A���g/�T�[�o�^�݌v�Ɠ��l�ɁA�f�[�^��M�� (ClientSource) ���K�v�ɉ����đ��M�� (ServerSink) �Ɛڑ���
�A�h�z�b�N�ȃf�[�^���W���s���܂��B����ɂ��f�[�^�Z���^�[�̊O���Ƀf�[�^�t���[��ڑ����邱�Ƃ��ł��A�܂���M����
��ɋ쓮���Ă����ԂłȂ��Ă��A�K�v�ȂƂ��ɂ����f�[�^�����W����^�p���s�����Ƃ��ł��܂��B

## �@�\

`flume-rips` �̓f�[�^�Z���^�[������t�@�C�A�E�H�[���⃍�[�h�o�����T����ĊO���Ƀf�[�^�𑗐M�ł���悤 Flume �C�x���g��
WebSockets��œ]�����܂��B

![flume-risp](doc/flume-risp1.png)

`ClientSource` �̓f�[�^���K�v�ɂȂ����Ƃ��� `ServerSink` �֐ڑ����܂��B`ServerSink` �͕����� `ClientSource` �C���X�^���X
�ɑ΂��ăf�[�^�𑗐M���邱�Ƃ��ł��܂��B

![flume-risp](doc/flume-risp2.png)

`ClientSource` ���ڑ����Ă��Ȃ��Ƃ��̃f�[�^�͕ۏ؂���܂���B

### WebSockets Event Delivery

Sink �� WebSocket �T�[�o�Ƃ��Ď�������Ă���AFlume �f�[�^�]����v������N���C�A���g����ڑ����邱�Ƃɂ���ăN���C�A���g
(�ʂ� Flume Agent) �ɑ΂��Ẵf�[�^�]�����J�n���܂��B

WebSockets �����̑I��ɂ���: Finagle �� WebSockets �������Ȃ��BNetty �̓o�[�W���� 4 �� WebSockets �n���h�V�F�C�N��
�������Ă��邪 Flume 1.6 �� Netty 3.5 �Ɉˑ����Ă���BGrizzly�AJetty �� WebSockets �N���C�A���g/�T�[�o���Ɏ�������B

## ����

* TODO: TLS (wss) �Ή�����B
* TODO: `ClientSink` �ڑ����ɃZ�b�V�����m��A�u�f���������Ă��Z�b�V�������ƂɈ��C�x���g���܂��͈�莞�ԃL���[�ɕۑ����Ă����B
* ����: �L���[���g���ďu�f����̈�莞�ԓ��̍Đڑ��ł̃f�[�^��ۏ؂��邩�B
